package com.aviraxp.adblocker.continued;

import android.content.res.Resources;
import android.content.res.XModuleResources;

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

    private Set<String> patterns;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> inetAddrClz = XposedHelpers.findClass("java.net.InetAddress", lpparam.classLoader);
        Class<?> inetSockAddrClz = XposedHelpers.findClass("java.net.InetSocketAddress", lpparam.classLoader);
        Class<?> socketClz = XposedHelpers.findClass("java.net.Socket", lpparam.classLoader);
        Class<?> ioBridgeClz = XposedHelpers.findClass("libcore.io.IoBridge", lpparam.classLoader);

        XposedBridge.hookAllConstructors(socketClz, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (null != param.args && param.args.length > 0) {
                    try {
                        Object obj = param.args[0];
                        String host = null;
                        if (null != obj && "java.lang.String".equals(obj.getClass().getName())) {
                            host = (String) obj;
                        } else if (null != obj && "java.lang.InetAddress".equals(obj.getClass().getName())) {
                            host = ((InetAddress) obj).getHostName();
                        }
                        if (host != null && patterns.contains(host)) {
                            param.args[0] = null;
                            param.setResult(new Object());
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                            }
                        }
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log(e);
                        }
                    }
                }
            }
        });

        XC_MethodHook inetAddrHookSingleResult = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && patterns.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log(e);
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String host = (String) param.args[0];
                    if (host != null && patterns.contains(host)) {
                        param.setResult(new Object());
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log(e);
                    }
                }
            }
        };
        XposedBridge.hookAllMethods(inetAddrClz, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(inetAddrClz, "getByName", inetAddrHookSingleResult);

        XposedBridge.hookAllMethods(inetSockAddrClz, "createUnresolved", inetAddrHookSingleResult);
        XposedBridge.hookAllConstructors(inetSockAddrClz, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (null != param.args && param.args.length > 0) {
                    try {
                        Object obj = param.args[0];
                        String host = null;
                        if (null != obj && "java.lang.String".equals(obj.getClass().getName())) {
                            host = (String) obj;
                        } else if (null != obj && "java.net.InetAddress".equals(obj.getClass().getName())) {
                            host = ((InetAddress) obj).getHostName();
                        }
                        if (host != null && patterns.contains(host)) {
                            param.args[0] = "localhost";
                            param.setResult(new Object());
                            param.setThrowable(new UnknownHostException());
                            if (BuildConfig.DEBUG) {
                                XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                            }
                        }
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log(e);
                        }
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
                    if (host != null && patterns.contains(host)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    } else if (ip != null && patterns.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + ip);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log(e);
                    }
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    InetAddress addr = (InetAddress) param.args[1];
                    String host = addr.getHostName();
                    String ip = addr.getHostAddress();
                    if (host != null && patterns.contains(host)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + host);
                        }
                    } else if (ip != null && patterns.contains(ip)) {
                        param.setResult(false);
                        param.setThrowable(new UnknownHostException());
                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("Hosts Block Success: " + lpparam.packageName + "/" + ip);
                        }
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log(e);
                    }
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
        String decoded = new String(array);
        String[] sUrls = decoded.split("\n");
        patterns = new HashSet<>();
        Collections.addAll(patterns, sUrls);
    }
}
