package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newScorecardEntry("code-161", "name-574")
        assertEquals(ScorecardEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScorecardEntry("code-161", "name-574")
        try {
            item.moveTo(ScorecardEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScorecardEntry("code-161", "name-574")
        item.status = ScorecardEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(ScorecardEntryStatus.PLANNED) }
    }
}
