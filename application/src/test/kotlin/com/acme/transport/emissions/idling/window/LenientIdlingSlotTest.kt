package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientIdlingSlotTest {
    @Test
    fun `starts in open`() {
        val item = newLenientIdlingSlot("code-183", "label-371")
        assertEquals(LenientIdlingSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientIdlingSlot("code-183", "label-371")
        try {
            item.moveTo(LenientIdlingSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientIdlingSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientIdlingSlot("code-183", "label-371")
        item.status = LenientIdlingSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientIdlingSlotStatus.OPEN) }
    }
}
