package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialAdjustment("code-878", "title-881")
        assertEquals(AccessorialAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAccessorialAdjustment("code-878", "title-881")
        try {
            item.moveTo(AccessorialAdjustmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialAdjustmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialAdjustment("code-878", "title-881")
        item.status = AccessorialAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialAdjustmentStatus.REQUESTED) }
    }
}
