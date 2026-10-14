package com.acme.transport.emissions.evcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEvChargeOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingEvChargeOverride("code-557", "label-897")
        assertEquals(PendingEvChargeOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingEvChargeOverride("code-557", "label-897")
        try {
            item.moveTo(PendingEvChargeOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEvChargeOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingEvChargeOverride("code-557", "label-897")
        item.status = PendingEvChargeOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingEvChargeOverrideStatus.REQUESTED) }
    }
}
