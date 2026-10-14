package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanEstimate("code-791", "title-88")
        assertEquals(ServicePlanEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newServicePlanEstimate("code-791", "title-88")
        try {
            item.moveTo(ServicePlanEstimateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanEstimateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanEstimate("code-791", "title-88")
        item.status = ServicePlanEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanEstimateStatus.CREATED) }
    }
}
