package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDockDoorReviewTest {
    @Test
    fun `starts in new`() {
        val item = newLenientDockDoorReview("code-904", "name-563")
        assertEquals(LenientDockDoorReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLenientDockDoorReview("code-904", "name-563")
        try {
            item.moveTo(LenientDockDoorReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDockDoorReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientDockDoorReview("code-904", "name-563")
        item.status = LenientDockDoorReviewStatus.FAILED
        assertFails("move") { item.moveTo(LenientDockDoorReviewStatus.NEW) }
    }
}
