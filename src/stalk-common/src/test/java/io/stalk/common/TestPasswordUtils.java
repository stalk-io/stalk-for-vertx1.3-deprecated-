package io.stalk.common;

import io.stalk.common.utils.PasswordUtils;

import java.security.MessageDigest;

import org.junit.Test;

public class TestPasswordUtils {

	@Test
	public void testEncrypt(){
		System.out.println(PasswordUtils.encrypt("xptmxmwnddlqslek."));
	}
	
}
