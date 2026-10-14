package com.acme.transport.linehaul.trailer.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerEntryTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerEntry("code-266", "name-585")
        assertEquals(TrailerEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTrailerEntry("code-266", "name-585")
        try {
            item.moveTo(TrailerEntryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerEntryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerEntry("code-266", "name-585")
        item.status = TrailerEntryStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerEntryStatus.CREATED) }
    }
}
