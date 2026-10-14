package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newDwellEntry("code-163", "name-139")
        assertEquals(DwellEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDwellEntry("code-163", "name-139")
        try {
            item.moveTo(DwellEntryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellEntryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDwellEntry("code-163", "name-139")
        item.status = DwellEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DwellEntryStatus.PROPOSED) }
    }
}
