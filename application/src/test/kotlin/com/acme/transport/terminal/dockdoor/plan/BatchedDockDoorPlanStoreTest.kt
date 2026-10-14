package com.acme.transport.terminal.dockdoor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDockDoorPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDockDoorPlanStore()
        val item = newBatchedDockDoorPlan("code-855", "title-445")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
