package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingClearanceAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingClearanceAdjustment("code-877", "title-309")
        assertEquals(RollingClearanceAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingClearanceAdjustment("code-877", "title-309")
        try {
            item.moveTo(RollingClearanceAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingClearanceAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingClearanceAdjustment("code-877", "title-309")
        item.status = RollingClearanceAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingClearanceAdjustmentStatus.REQUESTED) }
    }
}
