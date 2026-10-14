package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAccessorialAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalAccessorialAdjustment("code-422", "label-570")
        assertEquals(GlobalAccessorialAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalAccessorialAdjustment("code-422", "label-570")
        try {
            item.moveTo(GlobalAccessorialAdjustmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAccessorialAdjustmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalAccessorialAdjustment("code-422", "label-570")
        item.status = GlobalAccessorialAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalAccessorialAdjustmentStatus.REQUESTED) }
    }
}
