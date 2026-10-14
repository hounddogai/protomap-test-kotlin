package com.acme.transport.safety.violation.violationslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalViolationSlotTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalViolationSlot("code-407", "label-300")
        assertEquals(RegionalViolationSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRegionalViolationSlot("code-407", "label-300")
        try {
            item.moveTo(RegionalViolationSlotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalViolationSlotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalViolationSlot("code-407", "label-300")
        item.status = RegionalViolationSlotStatus.FAILED
        assertFails("move") { item.moveTo(RegionalViolationSlotStatus.NEW) }
    }
}
