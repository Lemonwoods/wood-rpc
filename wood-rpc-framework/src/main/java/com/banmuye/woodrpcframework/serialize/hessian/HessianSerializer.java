package com.banmuye.woodrpcframework.serialize.hessian;

import com.banmuye.woodrpccommon.exception.SerializeException;
import com.banmuye.woodrpcframework.serialize.Serializer;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayOutputStream = new ByteArrayInputStream(bytes)){
            HessianInput hessianInput = new HessianInput(byteArrayOutputStream);
            Object obj = hessianInput.readObject();
            return clazz.cast(obj);
        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }
    }
}
