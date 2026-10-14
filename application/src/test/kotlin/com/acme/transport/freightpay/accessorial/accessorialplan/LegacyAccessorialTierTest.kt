package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAccessorialTierTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyAccessorialTier("code-288", "title-752")
        assertEquals(LegacyAccessorialTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newLegacyAccessorialTier("code-288", "title-752")
        try {
            item.moveTo(LegacyAccessorialTierStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAccessorialTierStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyAccessorialTier("code-288", "title-752")
        item.status = LegacyAccessorialTierStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyAccessorialTierStatus.PLANNED) }
    }
}
