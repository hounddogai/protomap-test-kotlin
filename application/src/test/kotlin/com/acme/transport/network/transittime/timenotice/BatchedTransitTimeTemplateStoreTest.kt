package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTransitTimeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTransitTimeTemplateStore()
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
