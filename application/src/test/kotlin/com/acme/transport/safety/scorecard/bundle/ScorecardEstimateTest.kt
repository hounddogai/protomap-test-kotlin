package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newScorecardEstimate("code-609", "label-691")
        assertEquals(ScorecardEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScorecardEstimate("code-609", "label-691")
        try {
            item.moveTo(ScorecardEstimateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardEstimateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScorecardEstimate("code-609", "label-691")
        item.status = ScorecardEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(ScorecardEstimateStatus.DRAFT) }
    }
}
