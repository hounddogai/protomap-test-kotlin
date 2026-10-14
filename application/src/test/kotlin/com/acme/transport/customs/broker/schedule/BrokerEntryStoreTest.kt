package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerEntryStore()
        val item = newBrokerEntry("code-35", "name-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
