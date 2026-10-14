package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDwellReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultDwellReview("code-662", "title-436")
        assertEquals(DefaultDwellReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultDwellReview("code-662", "title-436")
        try {
            item.moveTo(DefaultDwellReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDwellReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultDwellReview("code-662", "title-436")
        item.status = DefaultDwellReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultDwellReviewStatus.REQUESTED) }
    }
}
