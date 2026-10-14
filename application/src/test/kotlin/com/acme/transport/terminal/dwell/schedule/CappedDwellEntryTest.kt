package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDwellEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedDwellEntry("code-208", "title-186")
        assertEquals(CappedDwellEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedDwellEntry("code-208", "title-186")
        try {
            item.moveTo(CappedDwellEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDwellEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedDwellEntry("code-208", "title-186")
        item.status = CappedDwellEntryStatus.CLOSED
        assertFails("move") { item.moveTo(CappedDwellEntryStatus.DRAFT) }
    }
}
