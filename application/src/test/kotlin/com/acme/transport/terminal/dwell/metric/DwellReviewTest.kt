package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellReviewTest {
    @Test
    fun `starts in new`() {
        val item = newDwellReview("code-977", "title-668")
        assertEquals(DwellReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDwellReview("code-977", "title-668")
        try {
            item.moveTo(DwellReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDwellReview("code-977", "title-668")
        item.status = DwellReviewStatus.FAILED
        assertFails("move") { item.moveTo(DwellReviewStatus.NEW) }
    }
}
