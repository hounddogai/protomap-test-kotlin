package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityBatchStore()
        val item = newCapacityBatch("code-273", "title-45")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
