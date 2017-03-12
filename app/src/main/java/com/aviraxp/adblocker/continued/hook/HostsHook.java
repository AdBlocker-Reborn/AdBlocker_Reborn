package com.aviraxp.adblocker.continued.hook;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class HostsHook {

    private static final String BLOCK_MESSAGE = "Blocked by AdBlocker Reborn: ";

    void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/hosts");
        byte[] array2 = XposedHelpers.assetAsByteArray(res, "blocklist/hosts_yhosts");
        String decoded = new String(array, "UTF-8");
        String decoded2 = new String(array2, "UTF-8");
        String decoded3 = decoded2.replace("127.0.0.1 ", "").replace("localhost", "workaround");
        String[] sUrls = decoded.split("\n");
        String[] sUrls2 = decoded3.split("\n");
        HookLoader.hostsList = new HashSet<>();
        Collections.addAll(HookLoader.hostsList, sUrls);
        Collections.addAll(HookLoader.hostsList, sUrls2);
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isHostsHookEnabled()) {
            return;
        }

        XC_MethodHook socketClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    Object obj = param.args[0];
                    String host = null;
                    if (obj instanceof String) {
                        host = (String) obj;
                    } else if (obj instanceof InetAddress) {
                        host = ((InetAddress) obj).getHostName();
                    }
                    if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                        param.setResult(null);
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    }
                }
            }
        };

        XC_MethodHook inetAddrHookSingleResult = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String host = (String) param.args[0];
                if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        XC_MethodHook inetSockAddrClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    Object obj = param.args[0];
                    String host = null;
                    if (obj instanceof String) {
                        host = (String) obj;
                    } else if (obj instanceof InetAddress) {
                        try {
                            host = ((InetAddress) obj).getHostName();
                        } catch (NetworkOnMainThreadException e) {
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
                            StrictMode.setThreadPolicy(policy);
                            host = ((InetAddress) obj).getHostName();
                        }
                    }
                    if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    }
                }
            }
        };

        XC_MethodHook ioBridgeHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                InetAddress address = (InetAddress) param.args[1];
                String host = address.getHostName();
                if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        XC_MethodHook blockGuardOsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                InetAddress address = (InetAddress) param.args[1];
                String host = address.getHostName();
                if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new SocketException(BLOCK_MESSAGE + host));
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        XC_MethodHook ioBridgeBooleanHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                InetAddress address = (InetAddress) param.args[1];
                String host = address.getHostName();
                if (host != null && !PreferencesHelper.whiteListElements().contains(host) && HookLoader.hostsList.contains(host)) {
                    param.setResult(false);
                    param.setThrowable(new ConnectException(BLOCK_MESSAGE + host));
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };


        Class<?> ioBridgeClz = XposedHelpers.findClass("libcore.io.IoBridge", lpparam.classLoader);
        Class<?> blockGuardOsClz = XposedHelpers.findClass("libcore.io.BlockGuardOs", lpparam.classLoader);

        XposedBridge.hookAllConstructors(Socket.class, socketClzHook);
        XposedBridge.hookAllConstructors(InetSocketAddress.class, inetSockAddrClzHook);
        XposedBridge.hookAllMethods(InetAddress.class, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(InetAddress.class, "getByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(InetSocketAddress.class, "createUnresolved", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(ioBridgeClz, "connect", ioBridgeHook);
        XposedBridge.hookAllMethods(ioBridgeClz, "connectErrno", ioBridgeHook);
        XposedBridge.hookAllMethods(ioBridgeClz, "isConnected", ioBridgeBooleanHook);
        XposedBridge.hookAllMethods(blockGuardOsClz, "connect", blockGuardOsHook);
    }
}
