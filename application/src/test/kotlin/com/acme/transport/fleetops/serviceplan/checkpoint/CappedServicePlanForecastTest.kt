package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServicePlanForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedServicePlanForecast("code-531", "title-607")
        assertEquals(CappedServicePlanForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCappedServicePlanForecast("code-531", "title-607")
        try {
            item.moveTo(CappedServicePlanForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServicePlanForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedServicePlanForecast("code-531", "title-607")
        item.status = CappedServicePlanForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedServicePlanForecastStatus.PROPOSED) }
    }
}
