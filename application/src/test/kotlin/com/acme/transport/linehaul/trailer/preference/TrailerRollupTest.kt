package com.acme.transport.linehaul.trailer.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerRollup("code-286", "name-523")
        assertEquals(TrailerRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newTrailerRollup("code-286", "name-523")
        try {
            item.moveTo(TrailerRollupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerRollupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerRollup("code-286", "name-523")
        item.status = TrailerRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerRollupStatus.PLANNED) }
    }
}
