package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaReviewTest {
    @Test
    fun `starts in new`() {
        val item = newServiceAreaReview("code-640", "title-778")
        assertEquals(ServiceAreaReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newServiceAreaReview("code-640", "title-778")
        try {
            item.moveTo(ServiceAreaReviewStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaReviewStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServiceAreaReview("code-640", "title-778")
        item.status = ServiceAreaReviewStatus.FAILED
        assertFails("move") { item.moveTo(ServiceAreaReviewStatus.NEW) }
    }
}
