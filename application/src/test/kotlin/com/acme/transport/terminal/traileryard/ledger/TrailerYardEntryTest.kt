package com.acme.transport.terminal.traileryard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardEntryTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerYardEntry("code-124", "name-624")
        assertEquals(TrailerYardEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTrailerYardEntry("code-124", "name-624")
        try {
            item.moveTo(TrailerYardEntryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardEntryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerYardEntry("code-124", "name-624")
        item.status = TrailerYardEntryStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerYardEntryStatus.CREATED) }
    }
}
