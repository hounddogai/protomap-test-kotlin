package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedBrokerRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedBrokerRecordStore()
        val item = newWeightedBrokerRecord("code-186", "name-621")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
