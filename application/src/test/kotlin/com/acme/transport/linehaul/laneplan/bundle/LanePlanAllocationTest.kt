package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanAllocation("code-893", "title-468")
        assertEquals(LanePlanAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLanePlanAllocation("code-893", "title-468")
        try {
            item.moveTo(LanePlanAllocationStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanAllocationStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanAllocation("code-893", "title-468")
        item.status = LanePlanAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanAllocationStatus.IDLE) }
    }
}
