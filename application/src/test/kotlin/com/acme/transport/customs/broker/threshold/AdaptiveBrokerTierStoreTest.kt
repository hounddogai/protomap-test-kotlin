package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveBrokerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveBrokerTierStore()
        val item = newAdaptiveBrokerTier("code-856", "name-701")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
