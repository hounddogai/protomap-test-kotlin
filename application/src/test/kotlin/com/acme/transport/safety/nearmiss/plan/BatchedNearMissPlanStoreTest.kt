package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedNearMissPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedNearMissPlanStore()
        val item = newBatchedNearMissPlan("code-885", "name-179")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
