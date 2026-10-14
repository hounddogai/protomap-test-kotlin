package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialTierTest {
    @Test
    fun `starts in planned`() {
        val item = newAccessorialTier("code-309", "label-866")
        assertEquals(AccessorialTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAccessorialTier("code-309", "label-866")
        try {
            item.moveTo(AccessorialTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAccessorialTier("code-309", "label-866")
        item.status = AccessorialTierStatus.CANCELLED
        assertFails("move") { item.moveTo(AccessorialTierStatus.PLANNED) }
    }
}
