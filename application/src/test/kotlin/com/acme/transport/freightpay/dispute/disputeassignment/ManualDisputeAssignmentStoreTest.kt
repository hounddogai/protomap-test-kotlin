package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDisputeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDisputeAssignmentStore()
        val item = newManualDisputeAssignment("code-250", "label-111")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
