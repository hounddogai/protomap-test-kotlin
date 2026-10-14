package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalClearanceReviewTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalClearanceReview("code-295", "label-450")
        assertEquals(RegionalClearanceReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalClearanceReview("code-295", "label-450")
        try {
            item.moveTo(RegionalClearanceReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalClearanceReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalClearanceReview("code-295", "label-450")
        item.status = RegionalClearanceReviewStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalClearanceReviewStatus.CREATED) }
    }
}
