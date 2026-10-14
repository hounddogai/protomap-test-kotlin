package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDockDoorAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedDockDoorAudit("code-358", "label-446")
        assertEquals(WeightedDockDoorAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newWeightedDockDoorAudit("code-358", "label-446")
        try {
            item.moveTo(WeightedDockDoorAuditStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDockDoorAuditStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedDockDoorAudit("code-358", "label-446")
        item.status = WeightedDockDoorAuditStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedDockDoorAuditStatus.IDLE) }
    }
}
