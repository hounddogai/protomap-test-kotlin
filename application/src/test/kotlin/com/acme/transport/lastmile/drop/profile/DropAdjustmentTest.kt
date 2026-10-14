package com.acme.transport.lastmile.drop.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDropAdjustment("code-495", "title-437")
        assertEquals(DropAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDropAdjustment("code-495", "title-437")
        try {
            item.moveTo(DropAdjustmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropAdjustmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropAdjustment("code-495", "title-437")
        item.status = DropAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(DropAdjustmentStatus.IDLE) }
    }
}
