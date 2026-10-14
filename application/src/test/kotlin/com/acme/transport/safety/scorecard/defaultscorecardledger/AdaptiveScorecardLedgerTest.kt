package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveScorecardLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveScorecardLedger("code-852", "name-219")
        assertEquals(AdaptiveScorecardLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveScorecardLedger("code-852", "name-219")
        try {
            item.moveTo(AdaptiveScorecardLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveScorecardLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveScorecardLedger("code-852", "name-219")
        item.status = AdaptiveScorecardLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveScorecardLedgerStatus.PROPOSED) }
    }
}
