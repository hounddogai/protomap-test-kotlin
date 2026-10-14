package com.acme.transport.safety.scorecard.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardAuditTest {
    @Test
    fun `starts in open`() {
        val item = newScorecardAudit("code-355", "title-867")
        assertEquals(ScorecardAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScorecardAudit("code-355", "title-867")
        try {
            item.moveTo(ScorecardAuditStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardAuditStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScorecardAudit("code-355", "title-867")
        item.status = ScorecardAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScorecardAuditStatus.OPEN) }
    }
}
