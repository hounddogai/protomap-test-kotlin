package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRelayReviewTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveRelayReview("code-535", "label-31")
        assertEquals(AdaptiveRelayReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveRelayReview("code-535", "label-31")
        try {
            item.moveTo(AdaptiveRelayReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRelayReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveRelayReview("code-535", "label-31")
        item.status = AdaptiveRelayReviewStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveRelayReviewStatus.NEW) }
    }
}
