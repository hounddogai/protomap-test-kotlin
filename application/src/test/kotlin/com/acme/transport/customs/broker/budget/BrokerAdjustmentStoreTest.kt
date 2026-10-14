package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerAdjustmentStore()
        val item = newBrokerAdjustment("code-82", "title-533")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
