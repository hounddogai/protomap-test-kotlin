package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRosterAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRosterAdjustmentStore()
        val item = newBatchedRosterAdjustment("code-829", "label-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
