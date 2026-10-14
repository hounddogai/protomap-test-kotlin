package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBrokerSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBrokerSlotStore()
        val item = newRollingBrokerSlot("code-606", "name-539")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
