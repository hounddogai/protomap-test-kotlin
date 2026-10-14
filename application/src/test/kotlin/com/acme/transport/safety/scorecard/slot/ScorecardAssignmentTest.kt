package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newScorecardAssignment("code-372", "label-711")
        assertEquals(ScorecardAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScorecardAssignment("code-372", "label-711")
        try {
            item.moveTo(ScorecardAssignmentStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardAssignmentStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScorecardAssignment("code-372", "label-711")
        item.status = ScorecardAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScorecardAssignmentStatus.PROPOSED) }
    }
}
