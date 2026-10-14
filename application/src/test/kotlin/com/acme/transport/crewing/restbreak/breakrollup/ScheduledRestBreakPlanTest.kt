package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRestBreakPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledRestBreakPlan("code-659", "title-53")
        assertEquals(ScheduledRestBreakPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledRestBreakPlan("code-659", "title-53")
        try {
            item.moveTo(ScheduledRestBreakPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRestBreakPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledRestBreakPlan("code-659", "title-53")
        item.status = ScheduledRestBreakPlanStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledRestBreakPlanStatus.IDLE) }
    }
}
