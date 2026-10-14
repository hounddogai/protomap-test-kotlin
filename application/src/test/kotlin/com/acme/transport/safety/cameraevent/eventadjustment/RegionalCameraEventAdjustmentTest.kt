package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCameraEventAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalCameraEventAdjustment("code-72", "name-999")
        assertEquals(RegionalCameraEventAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalCameraEventAdjustment("code-72", "name-999")
        try {
            item.moveTo(RegionalCameraEventAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCameraEventAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalCameraEventAdjustment("code-72", "name-999")
        item.status = RegionalCameraEventAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalCameraEventAdjustmentStatus.IDLE) }
    }
}
