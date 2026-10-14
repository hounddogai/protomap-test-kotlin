package com.acme.transport.kit.cache

import com.acme.transport.kit.clock.FixedClock
import com.acme.transport.kit.clock.StepClock
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheTest {
    @Test
    fun `evicts the oldest entry`() {
        val cache = newTtlCache<String>(2, 60, FixedClock(1000))
        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")
        assertEquals(2, cache.size(), "size")
    }

    @Test
    fun `forgets expired entries`() {
        val clock = StepClock(0, 100)
        val cache = newTtlCache<String>(8, 150, clock)
        cache.put("key", "value")
        assertEquals(0, cache.purgeExpired(), "purged early")
        assertEquals(1, cache.purgeExpired(), "purged late")
    }
}
