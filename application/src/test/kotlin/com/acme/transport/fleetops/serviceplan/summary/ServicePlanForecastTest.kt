package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanForecastTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanForecast("code-744", "label-44")
        assertEquals(ServicePlanForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newServicePlanForecast("code-744", "label-44")
        try {
            item.moveTo(ServicePlanForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanForecast("code-744", "label-44")
        item.status = ServicePlanForecastStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanForecastStatus.CREATED) }
    }
}
