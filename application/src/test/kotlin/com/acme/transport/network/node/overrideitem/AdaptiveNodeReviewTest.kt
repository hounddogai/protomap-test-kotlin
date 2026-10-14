package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNodeReviewTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveNodeReview("code-220", "name-728")
        assertEquals(AdaptiveNodeReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveNodeReview("code-220", "name-728")
        try {
            item.moveTo(AdaptiveNodeReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNodeReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveNodeReview("code-220", "name-728")
        item.status = AdaptiveNodeReviewStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveNodeReviewStatus.CREATED) }
    }
}
