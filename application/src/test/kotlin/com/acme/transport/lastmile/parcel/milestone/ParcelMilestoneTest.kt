package com.acme.transport.lastmile.parcel.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newParcelMilestone("code-600", "name-328")
        assertEquals(ParcelMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newParcelMilestone("code-600", "name-328")
        try {
            item.moveTo(ParcelMilestoneStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelMilestoneStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newParcelMilestone("code-600", "name-328")
        item.status = ParcelMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(ParcelMilestoneStatus.IDLE) }
    }
}
