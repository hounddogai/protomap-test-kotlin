package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBackhaulAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveBackhaulAdjustment("code-296", "label-493")
        assertEquals(AdaptiveBackhaulAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAdaptiveBackhaulAdjustment("code-296", "label-493")
        try {
            item.moveTo(AdaptiveBackhaulAdjustmentStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBackhaulAdjustmentStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveBackhaulAdjustment("code-296", "label-493")
        item.status = AdaptiveBackhaulAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveBackhaulAdjustmentStatus.IDLE) }
    }
}
