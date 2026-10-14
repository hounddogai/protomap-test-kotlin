package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBackhaulMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackBackhaulMilestone("code-870", "title-592")
        assertEquals(FallbackBackhaulMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackBackhaulMilestone("code-870", "title-592")
        try {
            item.moveTo(FallbackBackhaulMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBackhaulMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackBackhaulMilestone("code-870", "title-592")
        item.status = FallbackBackhaulMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackBackhaulMilestoneStatus.IDLE) }
    }
}
