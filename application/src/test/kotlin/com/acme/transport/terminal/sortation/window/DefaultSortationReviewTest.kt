package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultSortationReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultSortationReview("code-217", "label-588")
        assertEquals(DefaultSortationReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDefaultSortationReview("code-217", "label-588")
        try {
            item.moveTo(DefaultSortationReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultSortationReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultSortationReview("code-217", "label-588")
        item.status = DefaultSortationReviewStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultSortationReviewStatus.IDLE) }
    }
}
