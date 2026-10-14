package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationReviewTest {
    @Test
    fun `starts in idle`() {
        val item = newUtilizationReview("code-2", "name-65")
        assertEquals(UtilizationReviewStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newUtilizationReview("code-2", "name-65")
        try {
            item.moveTo(UtilizationReviewStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationReviewStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newUtilizationReview("code-2", "name-65")
        item.status = UtilizationReviewStatus.STOPPED
        assertFails("move") { item.moveTo(UtilizationReviewStatus.IDLE) }
    }
}
