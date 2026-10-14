package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newScorecardVariance("code-969", "title-588")
        assertEquals(ScorecardVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScorecardVariance("code-969", "title-588")
        try {
            item.moveTo(ScorecardVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScorecardVariance("code-969", "title-588")
        item.status = ScorecardVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(ScorecardVarianceStatus.PLANNED) }
    }
}
