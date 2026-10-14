package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryBrokerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryBrokerTierStore()
        val item = newPrimaryBrokerTier("code-558", "label-474")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
