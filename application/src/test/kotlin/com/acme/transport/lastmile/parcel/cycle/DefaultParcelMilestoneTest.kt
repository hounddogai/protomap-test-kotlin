package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultParcelMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultParcelMilestone("code-276", "label-958")
        assertEquals(DefaultParcelMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultParcelMilestone("code-276", "label-958")
        try {
            item.moveTo(DefaultParcelMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultParcelMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultParcelMilestone("code-276", "label-958")
        item.status = DefaultParcelMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultParcelMilestoneStatus.IDLE) }
    }
}
