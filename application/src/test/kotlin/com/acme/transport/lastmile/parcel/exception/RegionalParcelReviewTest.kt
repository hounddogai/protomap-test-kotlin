package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalParcelReviewTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalParcelReview("code-162", "name-857")
        assertEquals(RegionalParcelReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalParcelReview("code-162", "name-857")
        try {
            item.moveTo(RegionalParcelReviewStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalParcelReviewStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalParcelReview("code-162", "name-857")
        item.status = RegionalParcelReviewStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalParcelReviewStatus.CREATED) }
    }
}
