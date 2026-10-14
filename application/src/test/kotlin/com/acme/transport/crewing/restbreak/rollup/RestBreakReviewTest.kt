package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakReviewTest {
    @Test
    fun `starts in created`() {
        val item = newRestBreakReview("code-803", "title-577")
        assertEquals(RestBreakReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRestBreakReview("code-803", "title-577")
        try {
            item.moveTo(RestBreakReviewStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakReviewStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRestBreakReview("code-803", "title-577")
        item.status = RestBreakReviewStatus.RETIRED
        assertFails("move") { item.moveTo(RestBreakReviewStatus.CREATED) }
    }
}
