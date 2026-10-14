package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyServicePlanMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyServicePlanMetric("code-421", "label-206")
        assertEquals(LegacyServicePlanMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLegacyServicePlanMetric("code-421", "label-206")
        try {
            item.moveTo(LegacyServicePlanMetricStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyServicePlanMetricStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyServicePlanMetric("code-421", "label-206")
        item.status = LegacyServicePlanMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyServicePlanMetricStatus.PLANNED) }
    }
}
