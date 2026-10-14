package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCoachingLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newManualCoachingLedger("code-376", "title-586")
        assertEquals(ManualCoachingLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualCoachingLedger("code-376", "title-586")
        try {
            item.moveTo(ManualCoachingLedgerStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCoachingLedgerStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualCoachingLedger("code-376", "title-586")
        item.status = ManualCoachingLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualCoachingLedgerStatus.PLANNED) }
    }
}
