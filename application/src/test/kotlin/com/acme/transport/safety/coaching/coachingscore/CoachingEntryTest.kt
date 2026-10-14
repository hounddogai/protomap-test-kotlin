package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingEntryTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingEntry("code-687", "title-363")
        assertEquals(CoachingEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCoachingEntry("code-687", "title-363")
        try {
            item.moveTo(CoachingEntryStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingEntryStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingEntry("code-687", "title-363")
        item.status = CoachingEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingEntryStatus.OPEN) }
    }
}
