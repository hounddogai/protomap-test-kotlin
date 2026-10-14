package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newScorecardGroup("code-601", "name-240")
        assertEquals(ScorecardGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScorecardGroup("code-601", "name-240")
        try {
            item.moveTo(ScorecardGroupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardGroupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScorecardGroup("code-601", "name-240")
        item.status = ScorecardGroupStatus.CLOSED
        assertFails("move") { item.moveTo(ScorecardGroupStatus.DRAFT) }
    }
}
