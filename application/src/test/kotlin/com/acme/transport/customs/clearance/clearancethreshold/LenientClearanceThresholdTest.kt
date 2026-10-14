package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientClearanceThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientClearanceThreshold("code-523", "label-785")
        assertEquals(LenientClearanceThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLenientClearanceThreshold("code-523", "label-785")
        try {
            item.moveTo(LenientClearanceThresholdStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientClearanceThresholdStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientClearanceThreshold("code-523", "label-785")
        item.status = LenientClearanceThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(LenientClearanceThresholdStatus.IDLE) }
    }
}
