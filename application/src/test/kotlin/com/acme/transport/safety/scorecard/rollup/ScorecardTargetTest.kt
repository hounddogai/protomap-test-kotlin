package com.acme.transport.safety.scorecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardTargetTest {
    @Test
    fun `starts in new`() {
        val item = newScorecardTarget("code-339", "name-978")
        assertEquals(ScorecardTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScorecardTarget("code-339", "name-978")
        try {
            item.moveTo(ScorecardTargetStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardTargetStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScorecardTarget("code-339", "name-978")
        item.status = ScorecardTargetStatus.FAILED
        assertFails("move") { item.moveTo(ScorecardTargetStatus.NEW) }
    }
}
