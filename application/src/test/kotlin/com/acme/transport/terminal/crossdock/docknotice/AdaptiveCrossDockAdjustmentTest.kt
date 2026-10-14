package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveCrossDockAdjustment("code-460", "label-690")
        assertEquals(AdaptiveCrossDockAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveCrossDockAdjustment("code-460", "label-690")
        try {
            item.moveTo(AdaptiveCrossDockAdjustmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockAdjustmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveCrossDockAdjustment("code-460", "label-690")
        item.status = AdaptiveCrossDockAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveCrossDockAdjustmentStatus.OPEN) }
    }
}
