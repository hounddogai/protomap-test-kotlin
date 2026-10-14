package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyScorecardLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyScorecardLedger("code-884", "name-193")
        assertEquals(LegacyScorecardLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newLegacyScorecardLedger("code-884", "name-193")
        try {
            item.moveTo(LegacyScorecardLedgerStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyScorecardLedgerStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyScorecardLedger("code-884", "name-193")
        item.status = LegacyScorecardLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyScorecardLedgerStatus.IDLE) }
    }
}
