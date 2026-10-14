package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerProfile("code-785", "name-627")
        assertEquals(TrailerProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTrailerProfile("code-785", "name-627")
        try {
            item.moveTo(TrailerProfileStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerProfileStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerProfile("code-785", "name-627")
        item.status = TrailerProfileStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerProfileStatus.DRAFT) }
    }
}
