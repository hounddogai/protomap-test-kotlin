package com.acme.transport.linehaul.trailer.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerMilestone("code-848", "title-760")
        assertEquals(TrailerMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTrailerMilestone("code-848", "title-760")
        try {
            item.moveTo(TrailerMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerMilestone("code-848", "title-760")
        item.status = TrailerMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerMilestoneStatus.REQUESTED) }
    }
}
