package com.acme.transport.customs.clearance.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyClearanceWindowTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyClearanceWindow("code-810", "label-459")
        assertEquals(LegacyClearanceWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyClearanceWindow("code-810", "label-459")
        try {
            item.moveTo(LegacyClearanceWindowStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyClearanceWindowStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyClearanceWindow("code-810", "label-459")
        item.status = LegacyClearanceWindowStatus.FAILED
        assertFails("move") { item.moveTo(LegacyClearanceWindowStatus.NEW) }
    }
}
