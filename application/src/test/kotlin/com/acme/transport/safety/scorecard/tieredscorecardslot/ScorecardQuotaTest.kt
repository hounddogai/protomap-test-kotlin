package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newScorecardQuota("code-951", "label-947")
        assertEquals(ScorecardQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScorecardQuota("code-951", "label-947")
        try {
            item.moveTo(ScorecardQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScorecardQuota("code-951", "label-947")
        item.status = ScorecardQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScorecardQuotaStatus.PROPOSED) }
    }
}
