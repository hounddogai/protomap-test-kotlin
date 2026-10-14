package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingClearanceAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingClearanceAllocationStore()
        val item = newPendingClearanceAllocation("code-762", "title-684")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
