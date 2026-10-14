package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCameraEventAdjustmentTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackCameraEventAdjustment("code-566", "label-518")
        assertEquals(FallbackCameraEventAdjustmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackCameraEventAdjustment("code-566", "label-518")
        try {
            item.moveTo(FallbackCameraEventAdjustmentStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCameraEventAdjustmentStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackCameraEventAdjustment("code-566", "label-518")
        item.status = FallbackCameraEventAdjustmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackCameraEventAdjustmentStatus.OPEN) }
    }
}
