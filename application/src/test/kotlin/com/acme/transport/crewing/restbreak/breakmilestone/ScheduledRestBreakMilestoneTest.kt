package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        assertEquals(ScheduledRestBreakMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        try {
            item.moveTo(ScheduledRestBreakMilestoneStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakMilestoneStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        item.status = ScheduledRestBreakMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledRestBreakMilestoneStatus.CREATED) }
    }
}
