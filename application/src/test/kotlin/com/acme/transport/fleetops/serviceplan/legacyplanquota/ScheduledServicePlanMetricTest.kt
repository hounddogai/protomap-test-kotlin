package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServicePlanMetricTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledServicePlanMetric("code-533", "title-333")
        assertEquals(ScheduledServicePlanMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledServicePlanMetric("code-533", "title-333")
        try {
            item.moveTo(ScheduledServicePlanMetricStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServicePlanMetricStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledServicePlanMetric("code-533", "title-333")
        item.status = ScheduledServicePlanMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledServicePlanMetricStatus.OPEN) }
    }
}
