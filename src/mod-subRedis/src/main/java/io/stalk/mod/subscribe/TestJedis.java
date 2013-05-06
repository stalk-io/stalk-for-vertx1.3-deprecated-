package io.stalk.mod.subscribe;

import redis.clients.jedis.Jedis;

public class TestJedis {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Jedis jedis = new Jedis("ec2-67-202-33-0.compute-1.amazonaws.com", 8888);

		System.out.println(jedis.ping());
		System.out.println(jedis.info());
	}

}
