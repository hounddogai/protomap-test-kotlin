package com.acme.transport.customs.broker.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerBundleStore()
        val item = newBrokerBundle("code-594", "name-131")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
