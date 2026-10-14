package com.acme.transport.safety.scorecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardRecordTest {
    @Test
    fun `starts in new`() {
        val item = newScorecardRecord("code-850", "name-971")
        assertEquals(ScorecardRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScorecardRecord("code-850", "name-971")
        try {
            item.moveTo(ScorecardRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScorecardRecord("code-850", "name-971")
        item.status = ScorecardRecordStatus.FAILED
        assertFails("move") { item.moveTo(ScorecardRecordStatus.NEW) }
    }
}
