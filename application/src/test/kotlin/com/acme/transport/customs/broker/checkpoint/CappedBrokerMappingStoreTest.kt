package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBrokerMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBrokerMappingStore()
        val item = newCappedBrokerMapping("code-305", "label-643")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
