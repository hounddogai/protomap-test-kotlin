package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedScorecardProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedScorecardProposalStore()
        val item = newBatchedScorecardProposal("code-604", "label-913")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
