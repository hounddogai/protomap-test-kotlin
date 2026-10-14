package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newIdlingMilestone("code-775", "title-393")
        assertEquals(IdlingMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newIdlingMilestone("code-775", "title-393")
        try {
            item.moveTo(IdlingMilestoneStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingMilestoneStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIdlingMilestone("code-775", "title-393")
        item.status = IdlingMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(IdlingMilestoneStatus.PLANNED) }
    }
}
