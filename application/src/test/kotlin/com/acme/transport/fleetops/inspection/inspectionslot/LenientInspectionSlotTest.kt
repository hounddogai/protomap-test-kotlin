package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientInspectionSlot("code-789", "label-951")
        assertEquals(LenientInspectionSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientInspectionSlot("code-789", "label-951")
        try {
            item.moveTo(LenientInspectionSlotStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionSlotStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientInspectionSlot("code-789", "label-951")
        item.status = LenientInspectionSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientInspectionSlotStatus.PLANNED) }
    }
}
