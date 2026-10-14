package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRedeliverySlotTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientRedeliverySlot("code-413", "name-317")
        assertEquals(LenientRedeliverySlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLenientRedeliverySlot("code-413", "name-317")
        try {
            item.moveTo(LenientRedeliverySlotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRedeliverySlotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientRedeliverySlot("code-413", "name-317")
        item.status = LenientRedeliverySlotStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientRedeliverySlotStatus.PLANNED) }
    }
}
