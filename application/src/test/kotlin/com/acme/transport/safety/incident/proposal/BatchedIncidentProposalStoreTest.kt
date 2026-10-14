package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedIncidentProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedIncidentProposalStore()
        val item = newBatchedIncidentProposal("code-60", "label-458")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
