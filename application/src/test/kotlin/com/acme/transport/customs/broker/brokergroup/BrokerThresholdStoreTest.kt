package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerThresholdStore()
        val item = newBrokerThreshold("code-84", "title-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
