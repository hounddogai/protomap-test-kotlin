package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionSlot("code-394", "name-645")
        assertEquals(InspectionSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newInspectionSlot("code-394", "name-645")
        try {
            item.moveTo(InspectionSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionSlot("code-394", "name-645")
        item.status = InspectionSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionSlotStatus.PLANNED) }
    }
}
