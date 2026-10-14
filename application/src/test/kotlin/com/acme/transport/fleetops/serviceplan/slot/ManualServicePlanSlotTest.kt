package com.acme.transport.fleetops.serviceplan.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServicePlanSlotTest {
    @Test
    fun `starts in open`() {
        val item = newManualServicePlanSlot("code-326", "label-398")
        assertEquals(ManualServicePlanSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualServicePlanSlot("code-326", "label-398")
        try {
            item.moveTo(ManualServicePlanSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServicePlanSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualServicePlanSlot("code-326", "label-398")
        item.status = ManualServicePlanSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualServicePlanSlotStatus.OPEN) }
    }
}
