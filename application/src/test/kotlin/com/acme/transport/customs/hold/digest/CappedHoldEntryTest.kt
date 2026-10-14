package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHoldEntryTest {
    @Test
    fun `starts in created`() {
        val item = newCappedHoldEntry("code-950", "title-140")
        assertEquals(CappedHoldEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedHoldEntry("code-950", "title-140")
        try {
            item.moveTo(CappedHoldEntryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHoldEntryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedHoldEntry("code-950", "title-140")
        item.status = CappedHoldEntryStatus.RETIRED
        assertFails("move") { item.moveTo(CappedHoldEntryStatus.CREATED) }
    }
}
