package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServiceAreaAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackServiceAreaAdjustment("code-41", "name-216")
        assertEquals(FallbackServiceAreaAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackServiceAreaAdjustment("code-41", "name-216")
        try {
            item.moveTo(FallbackServiceAreaAdjustmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServiceAreaAdjustmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackServiceAreaAdjustment("code-41", "name-216")
        item.status = FallbackServiceAreaAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackServiceAreaAdjustmentStatus.IDLE) }
    }
}
