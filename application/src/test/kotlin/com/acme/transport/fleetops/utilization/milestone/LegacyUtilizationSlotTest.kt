package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyUtilizationSlotTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        assertEquals(LegacyUtilizationSlotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        try {
            item.moveTo(LegacyUtilizationSlotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyUtilizationSlotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        item.status = LegacyUtilizationSlotStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyUtilizationSlotStatus.IDLE) }
    }
}
