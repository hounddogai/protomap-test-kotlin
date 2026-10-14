package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackScorecardSlotTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackScorecardSlot("code-579", "title-633")
        assertEquals(FallbackScorecardSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackScorecardSlot("code-579", "title-633")
        try {
            item.moveTo(FallbackScorecardSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackScorecardSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackScorecardSlot("code-579", "title-633")
        item.status = FallbackScorecardSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackScorecardSlotStatus.OPEN) }
    }
}
