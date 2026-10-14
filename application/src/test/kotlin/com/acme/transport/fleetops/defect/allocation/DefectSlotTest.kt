package com.acme.transport.fleetops.defect.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectSlotTest {
    @Test
    fun `starts in idle`() {
        val item = newDefectSlot("code-11", "label-937")
        assertEquals(DefectSlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefectSlot("code-11", "label-937")
        try {
            item.moveTo(DefectSlotStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectSlotStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefectSlot("code-11", "label-937")
        item.status = DefectSlotStatus.STOPPED
        assertFails("move") { item.moveTo(DefectSlotStatus.IDLE) }
    }
}
