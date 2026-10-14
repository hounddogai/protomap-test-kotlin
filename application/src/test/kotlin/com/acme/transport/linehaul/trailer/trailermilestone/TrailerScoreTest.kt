package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerScoreTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerScore("code-983", "label-26")
        assertEquals(TrailerScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTrailerScore("code-983", "label-26")
        try {
            item.moveTo(TrailerScoreStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerScoreStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerScore("code-983", "label-26")
        item.status = TrailerScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerScoreStatus.OPEN) }
    }
}
