package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDefectSlotTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingDefectSlot("code-178", "label-177")
        assertEquals(PendingDefectSlotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingDefectSlot("code-178", "label-177")
        try {
            item.moveTo(PendingDefectSlotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDefectSlotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingDefectSlot("code-178", "label-177")
        item.status = PendingDefectSlotStatus.CLOSED
        assertFails("move") { item.moveTo(PendingDefectSlotStatus.DRAFT) }
    }
}
