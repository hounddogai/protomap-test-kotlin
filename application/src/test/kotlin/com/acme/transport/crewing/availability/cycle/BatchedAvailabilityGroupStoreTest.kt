package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAvailabilityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAvailabilityGroupStore()
        val item = newBatchedAvailabilityGroup("code-899", "label-255")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
