package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyServicePlanForecastTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyServicePlanForecast("code-548", "title-872")
        assertEquals(LegacyServicePlanForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLegacyServicePlanForecast("code-548", "title-872")
        try {
            item.moveTo(LegacyServicePlanForecastStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyServicePlanForecastStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyServicePlanForecast("code-548", "title-872")
        item.status = LegacyServicePlanForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyServicePlanForecastStatus.OPEN) }
    }
}
