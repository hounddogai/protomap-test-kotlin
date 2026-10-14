package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardTargetTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerYardTarget("code-665", "name-342")
        assertEquals(TrailerYardTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTrailerYardTarget("code-665", "name-342")
        try {
            item.moveTo(TrailerYardTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerYardTarget("code-665", "name-342")
        item.status = TrailerYardTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerYardTargetStatus.OPEN) }
    }
}
