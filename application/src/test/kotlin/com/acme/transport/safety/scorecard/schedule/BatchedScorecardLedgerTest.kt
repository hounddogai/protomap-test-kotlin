package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedScorecardLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedScorecardLedger("code-972", "name-917")
        assertEquals(BatchedScorecardLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedScorecardLedger("code-972", "name-917")
        try {
            item.moveTo(BatchedScorecardLedgerStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedScorecardLedgerStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedScorecardLedger("code-972", "name-917")
        item.status = BatchedScorecardLedgerStatus.FAILED
        assertFails("move") { item.moveTo(BatchedScorecardLedgerStatus.NEW) }
    }
}
