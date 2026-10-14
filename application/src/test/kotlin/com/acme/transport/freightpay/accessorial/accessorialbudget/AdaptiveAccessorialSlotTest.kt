package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAccessorialSlotTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveAccessorialSlot("code-346", "name-12")
        assertEquals(AdaptiveAccessorialSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveAccessorialSlot("code-346", "name-12")
        try {
            item.moveTo(AdaptiveAccessorialSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAccessorialSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveAccessorialSlot("code-346", "name-12")
        item.status = AdaptiveAccessorialSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveAccessorialSlotStatus.OPEN) }
    }
}
