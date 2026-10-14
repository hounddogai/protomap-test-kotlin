package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newCutoffMilestone("code-176", "name-783")
        assertEquals(CutoffMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCutoffMilestone("code-176", "name-783")
        try {
            item.moveTo(CutoffMilestoneStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffMilestoneStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCutoffMilestone("code-176", "name-783")
        item.status = CutoffMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(CutoffMilestoneStatus.IDLE) }
    }
}
