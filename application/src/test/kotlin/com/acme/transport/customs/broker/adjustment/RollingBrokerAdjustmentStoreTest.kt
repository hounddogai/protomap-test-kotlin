package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBrokerAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBrokerAdjustmentStore()
        val item = newRollingBrokerAdjustment("code-16", "name-175")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
