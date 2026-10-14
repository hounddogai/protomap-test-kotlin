package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionReviewTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionReview("code-216", "name-637")
        assertEquals(InspectionReviewStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newInspectionReview("code-216", "name-637")
        try {
            item.moveTo(InspectionReviewStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionReviewStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionReview("code-216", "name-637")
        item.status = InspectionReviewStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionReviewStatus.CREATED) }
    }
}
