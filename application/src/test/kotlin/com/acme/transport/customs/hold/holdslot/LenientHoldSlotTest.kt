package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHoldSlotTest {
    @Test
    fun `starts in open`() {
        val item = newLenientHoldSlot("code-657", "name-98")
        assertEquals(LenientHoldSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLenientHoldSlot("code-657", "name-98")
        try {
            item.moveTo(LenientHoldSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHoldSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientHoldSlot("code-657", "name-98")
        item.status = LenientHoldSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientHoldSlotStatus.OPEN) }
    }
}
