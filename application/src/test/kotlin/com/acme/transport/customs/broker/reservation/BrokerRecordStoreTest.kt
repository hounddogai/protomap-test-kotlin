package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerRecordStore()
        val item = newBrokerRecord("code-495", "label-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
