package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDisputeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDisputeSnapshotStore()
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
