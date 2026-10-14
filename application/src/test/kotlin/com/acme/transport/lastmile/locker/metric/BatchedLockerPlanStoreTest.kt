package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLockerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLockerPlanStore()
        val item = newBatchedLockerPlan("code-954", "label-870")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
