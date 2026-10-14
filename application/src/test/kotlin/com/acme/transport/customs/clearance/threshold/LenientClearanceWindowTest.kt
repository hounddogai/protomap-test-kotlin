package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientClearanceWindowTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientClearanceWindow("code-930", "label-152")
        assertEquals(LenientClearanceWindowStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLenientClearanceWindow("code-930", "label-152")
        try {
            item.moveTo(LenientClearanceWindowStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientClearanceWindowStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientClearanceWindow("code-930", "label-152")
        item.status = LenientClearanceWindowStatus.STOPPED
        assertFails("move") { item.moveTo(LenientClearanceWindowStatus.IDLE) }
    }
}
