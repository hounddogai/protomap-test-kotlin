package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newClearanceAdjustment("code-641", "title-255")
        assertEquals(ClearanceAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newClearanceAdjustment("code-641", "title-255")
        try {
            item.moveTo(ClearanceAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newClearanceAdjustment("code-641", "title-255")
        item.status = ClearanceAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(ClearanceAdjustmentStatus.REQUESTED) }
    }
}
