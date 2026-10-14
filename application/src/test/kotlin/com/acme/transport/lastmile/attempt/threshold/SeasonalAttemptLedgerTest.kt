package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAttemptLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalAttemptLedger("code-297", "title-111")
        assertEquals(SeasonalAttemptLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSeasonalAttemptLedger("code-297", "title-111")
        try {
            item.moveTo(SeasonalAttemptLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAttemptLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalAttemptLedger("code-297", "title-111")
        item.status = SeasonalAttemptLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalAttemptLedgerStatus.IDLE) }
    }
}
