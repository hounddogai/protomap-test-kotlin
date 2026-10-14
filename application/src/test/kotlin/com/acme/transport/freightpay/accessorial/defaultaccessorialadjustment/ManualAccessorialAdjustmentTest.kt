package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAccessorialAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        assertEquals(ManualAccessorialAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        try {
            item.moveTo(ManualAccessorialAdjustmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAccessorialAdjustmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        item.status = ManualAccessorialAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(ManualAccessorialAdjustmentStatus.IDLE) }
    }
}
