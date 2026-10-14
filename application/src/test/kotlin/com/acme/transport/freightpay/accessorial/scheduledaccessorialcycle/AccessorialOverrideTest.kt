package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newAccessorialOverride("code-78", "title-880")
        assertEquals(AccessorialOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAccessorialOverride("code-78", "title-880")
        try {
            item.moveTo(AccessorialOverrideStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialOverrideStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAccessorialOverride("code-78", "title-880")
        item.status = AccessorialOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(AccessorialOverrideStatus.DRAFT) }
    }
}
