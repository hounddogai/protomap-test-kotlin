package com.acme.transport.customs.broker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBrokerSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBrokerSlotStore()
        val item = newCappedBrokerSlot("code-207", "name-151")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
