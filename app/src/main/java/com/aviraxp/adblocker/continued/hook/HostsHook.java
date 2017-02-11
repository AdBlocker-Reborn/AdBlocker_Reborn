package com.aviraxp.adblocker.continued.hook;

import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.aviraxp.adblocker.continued.hook.HookLoader.hostsList;

class HostsHook {

    private static final String BLOCK_MESSAGE = "Blocked by AdBlocker Reborn: ";

    static void init(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/hosts");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        hostsList = new HashSet<>();
        Collections.addAll(hostsList, sUrls);
    }

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isHostsHookEnabled() || PreferencesHelper.disabledApps().contains(lpparam.packageName)) {
            return;
        }

        Class<?> inetAddrClz = XposedHelpers.findClass("java.net.InetAddress", lpparam.classLoader);
        Class<?> inetSockAddrClz = XposedHelpers.findClass("java.net.InetSocketAddress", lpparam.classLoader);
        Class<?> socketClz = XposedHelpers.findClass("java.net.Socket", lpparam.classLoader);
        Class<?> ioBridgeClz = XposedHelpers.findClass("libcore.io.IoBridge", lpparam.classLoader);

        XC_MethodHook socketClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                        Object obj = param.args[0];
                        String host = null;
                        if (obj.getClass().getName().equals("java.lang.String")) {
                            host = (String) obj;
                        } else if (obj.getClass().getName().equals("java.lang.InetAddress")) {
                            host = ((InetAddress) obj).getHostName();
                        }
                        if (host != null && hostsList.contains(host)) {
                            param.args[0] = null;
                            param.setResult(new Object());
                            LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                        }
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }
        };

        XC_MethodHook inetAddrHookSingleResult = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }
        };

        XC_MethodHook inetSockAddrClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                        Object obj = param.args[0];
                        String host = null;
                        if (obj.getClass().getName().equals("java.lang.String")) {
                            host = (String) obj;
                        } else if (obj.getClass().getName().equals("java.lang.InetAddress")) {
                            host = ((InetAddress) obj).getHostName();
                        }
                        if (host != null && hostsList.contains(host)) {
                            param.args[0] = "localhost";
                            param.setResult(new Object());
                            param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                            LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                        }
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }
        };

        XC_MethodHook ioBridgeHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    InetAddress addr = (InetAddress) param.args[1];
                    String host = addr.getHostName();
                    String ip = addr.getHostAddress();
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                    } else if (ip != null && hostsList.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + ip));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + ip, true);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    InetAddress addr = (InetAddress) param.args[1];
                    String host = addr.getHostName();
                    String ip = addr.getHostAddress();
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host, true);
                    } else if (ip != null && hostsList.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException(BLOCK_MESSAGE + ip));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + ip, true);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t, false);
                }
            }
        };

        XposedBridge.hookAllConstructors(socketClz, socketClzHook);
        XposedBridge.hookAllConstructors(inetSockAddrClz, inetSockAddrClzHook);
        XposedBridge.hookAllMethods(inetAddrClz, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(inetAddrClz, "getByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(inetSockAddrClz, "createUnresolved", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(ioBridgeClz, "connect", ioBridgeHook);
        XposedBridge.hookAllMethods(ioBridgeClz, "connectErrno", ioBridgeHook);
    }
}
