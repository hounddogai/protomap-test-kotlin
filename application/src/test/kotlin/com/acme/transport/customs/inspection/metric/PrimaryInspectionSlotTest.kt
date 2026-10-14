package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionSlotTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryInspectionSlot("code-41", "label-926")
        assertEquals(PrimaryInspectionSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPrimaryInspectionSlot("code-41", "label-926")
        try {
            item.moveTo(PrimaryInspectionSlotStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionSlotStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryInspectionSlot("code-41", "label-926")
        item.status = PrimaryInspectionSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryInspectionSlotStatus.OPEN) }
    }
}
