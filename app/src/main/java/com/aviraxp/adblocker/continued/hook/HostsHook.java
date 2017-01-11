package com.aviraxp.adblocker.continued.hook;

import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HostsHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> hostsList;
    private Set<String> whiteList;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!PreferencesHelper.isHostsHookEnabled()) {
            return;
        }

        if (whiteList.contains(lpparam.packageName)) {
            return;
        }

        Class<?> inetAddrClz = XposedHelpers.findClass("java.net.InetAddress", lpparam.classLoader);
        Class<?> inetSockAddrClz = XposedHelpers.findClass("java.net.InetSocketAddress", lpparam.classLoader);
        Class<?> socketClz = XposedHelpers.findClass("java.net.Socket", lpparam.classLoader);
        Class<?> ioBridgeClz = XposedHelpers.findClass("libcore.io.IoBridge", lpparam.classLoader);

        XposedBridge.hookAllConstructors(socketClz, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    try {
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
                            LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    } catch (Throwable t) {
                        LogUtils.logRecord(t);
                    }
                }
            }
        });

        XC_MethodHook inetAddrHookSingleResult = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t);
                }
            }
        };
        XposedBridge.hookAllMethods(inetAddrClz, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(inetAddrClz, "getByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(inetSockAddrClz, "createUnresolved", inetAddrHookSingleResult);

        XposedBridge.hookAllConstructors(inetSockAddrClz, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args != null && param.args.length > 0 && param.args[0] != null) {
                    try {
                        Object obj = param.args[0];
                        String host = null;
                        if (obj.getClass().getName().equals("java.lang.String")) {
                            host = (String) obj;
                        } else if (obj.getClass().getName().equals("java.lang.InetAddress")) {
                            host = ((InetAddress) obj).getHostName();
                        }
                        if (host != null && hostsList.contains(host)) {
                            param.args[0] = "0.0.0.0";
                            param.setResult(new Object());
                            param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + host));
                            LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    } catch (Throwable t) {
                        LogUtils.logRecord(t);
                    }
                }
            }
        });

        XC_MethodHook ioBridgeHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    InetAddress addr = (InetAddress) param.args[1];
                    String host = addr.getHostName();
                    String ip = addr.getHostAddress();
                    if (host != null && hostsList.contains(host)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    } else if (ip != null && hostsList.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + ip));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + ip);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t);
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
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + host));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                    } else if (ip != null && hostsList.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException("Blocked by ADBlocker Continued: " + ip));
                        LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + ip);
                    }
                } catch (Throwable t) {
                    LogUtils.logRecord(t);
                }
            }
        };
        XposedBridge.hookAllMethods(ioBridgeClz, "connect", ioBridgeHook);
        XposedBridge.hookAllMethods(ioBridgeClz, "connectErrno", ioBridgeHook);
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/hosts");
        byte[] array2 = XposedHelpers.assetAsByteArray(res, "whitelist/urlapp");
        String decoded = new String(array, "UTF-8");
        String decoded2 = new String(array2, "UTF-8");
        String[] sUrls = decoded.split("\n");
        String[] sUrls2 = decoded2.split("\n");
        hostsList = new HashSet<>();
        whiteList = new HashSet<>();
        Collections.addAll(hostsList, sUrls);
        Collections.addAll(whiteList, sUrls2);
    }
}
