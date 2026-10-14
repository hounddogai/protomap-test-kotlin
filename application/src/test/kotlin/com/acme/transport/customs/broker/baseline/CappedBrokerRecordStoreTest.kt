package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBrokerRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBrokerRecordStore()
        val item = newCappedBrokerRecord("code-717", "title-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
