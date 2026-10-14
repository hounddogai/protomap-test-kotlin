package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedServiceAreaEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedServiceAreaEntryStore()
        val item = newBatchedServiceAreaEntry("code-204", "label-753")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
