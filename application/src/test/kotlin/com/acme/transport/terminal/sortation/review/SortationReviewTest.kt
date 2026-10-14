package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newSortationReview("code-358", "name-795")
        assertEquals(SortationReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSortationReview("code-358", "name-795")
        try {
            item.moveTo(SortationReviewStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationReviewStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSortationReview("code-358", "name-795")
        item.status = SortationReviewStatus.STOPPED
        assertFails("move") { item.moveTo(SortationReviewStatus.IDLE) }
    }
}
