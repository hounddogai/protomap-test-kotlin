package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCapacityThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedCapacityThreshold("code-664", "label-333")
        assertEquals(CappedCapacityThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCappedCapacityThreshold("code-664", "label-333")
        try {
            item.moveTo(CappedCapacityThresholdStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCapacityThresholdStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedCapacityThreshold("code-664", "label-333")
        item.status = CappedCapacityThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(CappedCapacityThresholdStatus.IDLE) }
    }
}
