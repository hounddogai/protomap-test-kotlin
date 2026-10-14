package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCutoffEntryTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        assertEquals(PrimaryCutoffEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        try {
            item.moveTo(PrimaryCutoffEntryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCutoffEntryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        item.status = PrimaryCutoffEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryCutoffEntryStatus.OPEN) }
    }
}
