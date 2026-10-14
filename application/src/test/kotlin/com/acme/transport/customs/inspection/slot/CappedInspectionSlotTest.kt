package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedInspectionSlot("code-991", "name-144")
        assertEquals(CappedInspectionSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedInspectionSlot("code-991", "name-144")
        try {
            item.moveTo(CappedInspectionSlotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionSlotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedInspectionSlot("code-991", "name-144")
        item.status = CappedInspectionSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedInspectionSlotStatus.REQUESTED) }
    }
}
