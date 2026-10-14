package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockReview("code-157", "title-799")
        assertEquals(CrossDockReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCrossDockReview("code-157", "title-799")
        try {
            item.moveTo(CrossDockReviewStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockReviewStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockReview("code-157", "title-799")
        item.status = CrossDockReviewStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockReviewStatus.IDLE) }
    }
}
