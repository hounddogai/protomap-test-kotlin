package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialReviewTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledAccessorialReview("code-511", "label-460")
        assertEquals(ScheduledAccessorialReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledAccessorialReview("code-511", "label-460")
        try {
            item.moveTo(ScheduledAccessorialReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledAccessorialReview("code-511", "label-460")
        item.status = ScheduledAccessorialReviewStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledAccessorialReviewStatus.CREATED) }
    }
}
