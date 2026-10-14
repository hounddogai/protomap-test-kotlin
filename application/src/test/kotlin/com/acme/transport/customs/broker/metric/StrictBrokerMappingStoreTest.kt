package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBrokerMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBrokerMappingStore()
        val item = newStrictBrokerMapping("code-870", "label-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
