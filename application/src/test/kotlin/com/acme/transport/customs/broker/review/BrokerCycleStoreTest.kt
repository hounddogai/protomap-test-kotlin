package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerCycleStore()
        val item = newBrokerCycle("code-688", "name-450")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
