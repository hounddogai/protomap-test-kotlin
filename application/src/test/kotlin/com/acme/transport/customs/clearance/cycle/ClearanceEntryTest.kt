package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceEntry("code-679", "name-126")
        assertEquals(ClearanceEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newClearanceEntry("code-679", "name-126")
        try {
            item.moveTo(ClearanceEntryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceEntryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceEntry("code-679", "name-126")
        item.status = ClearanceEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceEntryStatus.PROPOSED) }
    }
}
