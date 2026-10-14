package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerTierStore()
        val item = newBrokerTier("code-262", "name-335")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
