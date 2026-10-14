package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newScorecardRollup("code-402", "label-311")
        assertEquals(ScorecardRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newScorecardRollup("code-402", "label-311")
        try {
            item.moveTo(ScorecardRollupStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardRollupStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScorecardRollup("code-402", "label-311")
        item.status = ScorecardRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScorecardRollupStatus.PROPOSED) }
    }
}
