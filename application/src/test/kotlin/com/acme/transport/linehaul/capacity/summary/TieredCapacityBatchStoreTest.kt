package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCapacityBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCapacityBatchStore()
        val item = newTieredCapacityBatch("code-850", "title-123")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
