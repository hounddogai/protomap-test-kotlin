package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualScorecardAuditTest {
    @Test
    fun `starts in open`() {
        val item = newManualScorecardAudit("code-547", "name-476")
        assertEquals(ManualScorecardAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newManualScorecardAudit("code-547", "name-476")
        try {
            item.moveTo(ManualScorecardAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualScorecardAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualScorecardAudit("code-547", "name-476")
        item.status = ManualScorecardAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualScorecardAuditStatus.OPEN) }
    }
}
