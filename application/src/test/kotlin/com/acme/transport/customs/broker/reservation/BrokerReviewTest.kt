package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerReviewTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerReview("code-899", "title-923")
        assertEquals(BrokerReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBrokerReview("code-899", "title-923")
        try {
            item.moveTo(BrokerReviewStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerReviewStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerReview("code-899", "title-923")
        item.status = BrokerReviewStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerReviewStatus.CREATED) }
    }
}
