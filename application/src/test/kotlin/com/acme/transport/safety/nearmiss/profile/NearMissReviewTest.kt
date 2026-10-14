package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissReviewTest {
    @Test
    fun `starts in open`() {
        val item = newNearMissReview("code-935", "name-141")
        assertEquals(NearMissReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newNearMissReview("code-935", "name-141")
        try {
            item.moveTo(NearMissReviewStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissReviewStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newNearMissReview("code-935", "name-141")
        item.status = NearMissReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(NearMissReviewStatus.OPEN) }
    }
}
