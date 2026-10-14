package com.acme.transport.terminal.dockdoor.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorReview("code-869", "name-879")
        assertEquals(DockDoorReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDockDoorReview("code-869", "name-879")
        try {
            item.moveTo(DockDoorReviewStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorReviewStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorReview("code-869", "name-879")
        item.status = DockDoorReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorReviewStatus.PROPOSED) }
    }
}
