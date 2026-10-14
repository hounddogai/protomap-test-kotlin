package com.acme.transport.safety.scorecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyScorecardVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyScorecardVariance("code-519", "name-643")
        assertEquals(LegacyScorecardVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyScorecardVariance("code-519", "name-643")
        try {
            item.moveTo(LegacyScorecardVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyScorecardVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyScorecardVariance("code-519", "name-643")
        item.status = LegacyScorecardVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyScorecardVarianceStatus.PROPOSED) }
    }
}
