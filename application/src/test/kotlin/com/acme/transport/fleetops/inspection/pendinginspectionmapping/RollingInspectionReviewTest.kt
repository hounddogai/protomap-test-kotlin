package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingInspectionReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingInspectionReview("code-732", "name-118")
        assertEquals(RollingInspectionReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingInspectionReview("code-732", "name-118")
        try {
            item.moveTo(RollingInspectionReviewStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingInspectionReviewStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingInspectionReview("code-732", "name-118")
        item.status = RollingInspectionReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingInspectionReviewStatus.REQUESTED) }
    }
}
