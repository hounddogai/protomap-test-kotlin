package com.acme.transport.network.cutoff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newCutoffEntry("code-58", "name-519")
        assertEquals(CutoffEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCutoffEntry("code-58", "name-519")
        try {
            item.moveTo(CutoffEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCutoffEntry("code-58", "name-519")
        item.status = CutoffEntryStatus.CLOSED
        assertFails("move") { item.moveTo(CutoffEntryStatus.DRAFT) }
    }
}
