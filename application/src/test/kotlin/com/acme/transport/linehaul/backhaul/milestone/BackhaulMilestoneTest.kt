package com.acme.transport.linehaul.backhaul.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newBackhaulMilestone("code-573", "label-294")
        assertEquals(BackhaulMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBackhaulMilestone("code-573", "label-294")
        try {
            item.moveTo(BackhaulMilestoneStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulMilestoneStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBackhaulMilestone("code-573", "label-294")
        item.status = BackhaulMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(BackhaulMilestoneStatus.OPEN) }
    }
}
