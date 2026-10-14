package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalScorecardAuditTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalScorecardAudit("code-129", "name-806")
        assertEquals(RegionalScorecardAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalScorecardAudit("code-129", "name-806")
        try {
            item.moveTo(RegionalScorecardAuditStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalScorecardAuditStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalScorecardAudit("code-129", "name-806")
        item.status = RegionalScorecardAuditStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalScorecardAuditStatus.CREATED) }
    }
}
