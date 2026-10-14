package com.acme.transport.fleetops.serviceplan.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanReview("code-469", "label-197")
        assertEquals(ServicePlanReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newServicePlanReview("code-469", "label-197")
        try {
            item.moveTo(ServicePlanReviewStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanReviewStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanReview("code-469", "label-197")
        item.status = ServicePlanReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanReviewStatus.PLANNED) }
    }
}
