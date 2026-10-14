package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceSlotTest {
    @Test
    fun `starts in open`() {
        val item = newClearanceSlot("code-410", "name-69")
        assertEquals(ClearanceSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newClearanceSlot("code-410", "name-69")
        try {
            item.moveTo(ClearanceSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newClearanceSlot("code-410", "name-69")
        item.status = ClearanceSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ClearanceSlotStatus.OPEN) }
    }
}
