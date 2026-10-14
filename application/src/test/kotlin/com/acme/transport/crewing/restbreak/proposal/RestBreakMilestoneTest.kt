package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newRestBreakMilestone("code-830", "title-383")
        assertEquals(RestBreakMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRestBreakMilestone("code-830", "title-383")
        try {
            item.moveTo(RestBreakMilestoneStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakMilestoneStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRestBreakMilestone("code-830", "title-383")
        item.status = RestBreakMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(RestBreakMilestoneStatus.PLANNED) }
    }
}
