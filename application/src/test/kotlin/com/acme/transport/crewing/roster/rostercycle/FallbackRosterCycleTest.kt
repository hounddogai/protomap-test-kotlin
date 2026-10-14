package com.acme.transport.crewing.roster.rostercycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRosterCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackRosterCycle("code-238", "title-280")
        assertEquals(FallbackRosterCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackRosterCycle("code-238", "title-280")
        try {
            item.moveTo(FallbackRosterCycleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRosterCycleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackRosterCycle("code-238", "title-280")
        item.status = FallbackRosterCycleStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackRosterCycleStatus.DRAFT) }
    }
}
