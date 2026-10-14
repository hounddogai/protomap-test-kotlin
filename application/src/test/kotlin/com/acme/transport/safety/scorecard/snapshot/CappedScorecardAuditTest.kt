package com.acme.transport.safety.scorecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedScorecardAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedScorecardAudit("code-643", "name-157")
        assertEquals(CappedScorecardAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedScorecardAudit("code-643", "name-157")
        try {
            item.moveTo(CappedScorecardAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedScorecardAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedScorecardAudit("code-643", "name-157")
        item.status = CappedScorecardAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedScorecardAuditStatus.PLANNED) }
    }
}
