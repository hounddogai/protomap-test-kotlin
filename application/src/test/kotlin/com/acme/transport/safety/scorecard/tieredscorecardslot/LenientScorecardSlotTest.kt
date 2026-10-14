package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientScorecardSlotTest {
    @Test
    fun `starts in open`() {
        val item = newLenientScorecardSlot("code-115", "name-839")
        assertEquals(LenientScorecardSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientScorecardSlot("code-115", "name-839")
        try {
            item.moveTo(LenientScorecardSlotStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientScorecardSlotStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientScorecardSlot("code-115", "name-839")
        item.status = LenientScorecardSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientScorecardSlotStatus.OPEN) }
    }
}
