package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultScorecardAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultScorecardAssignment("code-21", "name-788")
        assertEquals(DefaultScorecardAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultScorecardAssignment("code-21", "name-788")
        try {
            item.moveTo(DefaultScorecardAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultScorecardAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultScorecardAssignment("code-21", "name-788")
        item.status = DefaultScorecardAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultScorecardAssignmentStatus.PROPOSED) }
    }
}
