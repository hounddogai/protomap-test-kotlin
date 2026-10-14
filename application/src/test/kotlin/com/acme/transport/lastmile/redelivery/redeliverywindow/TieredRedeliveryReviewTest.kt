package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRedeliveryReviewTest {
    @Test
    fun `starts in created`() {
        val item = newTieredRedeliveryReview("code-873", "label-749")
        assertEquals(TieredRedeliveryReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredRedeliveryReview("code-873", "label-749")
        try {
            item.moveTo(TieredRedeliveryReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRedeliveryReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredRedeliveryReview("code-873", "label-749")
        item.status = TieredRedeliveryReviewStatus.RETIRED
        assertFails("move") { item.moveTo(TieredRedeliveryReviewStatus.CREATED) }
    }
}
