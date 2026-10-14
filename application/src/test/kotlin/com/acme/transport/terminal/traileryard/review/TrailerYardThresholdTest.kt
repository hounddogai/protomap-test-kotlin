package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerYardThreshold("code-702", "title-373")
        assertEquals(TrailerYardThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newTrailerYardThreshold("code-702", "title-373")
        try {
            item.moveTo(TrailerYardThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerYardThreshold("code-702", "title-373")
        item.status = TrailerYardThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerYardThresholdStatus.DRAFT) }
    }
}
