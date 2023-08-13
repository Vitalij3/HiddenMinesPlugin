package me.salatosik.hiddenminesplugin.utils;

@FunctionalInterface
public interface CommonFunctionThrowsException {
    void invoke() throws Exception;
}
