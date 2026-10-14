package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryHoldAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        assertEquals(PrimaryHoldAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        try {
            item.moveTo(PrimaryHoldAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryHoldAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryHoldAdjustment("code-141", "name-59")
        item.status = PrimaryHoldAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryHoldAdjustmentStatus.IDLE) }
    }
}
