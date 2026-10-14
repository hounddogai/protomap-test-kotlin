package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRestBreakMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyRestBreakMilestone("code-783", "name-483")
        assertEquals(LegacyRestBreakMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newLegacyRestBreakMilestone("code-783", "name-483")
        try {
            item.moveTo(LegacyRestBreakMilestoneStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRestBreakMilestoneStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyRestBreakMilestone("code-783", "name-483")
        item.status = LegacyRestBreakMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyRestBreakMilestoneStatus.DRAFT) }
    }
}
