package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalEvChargeMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalEvChargeMetric("code-419", "title-369")
        assertEquals(RegionalEvChargeMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalEvChargeMetric("code-419", "title-369")
        try {
            item.moveTo(RegionalEvChargeMetricStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalEvChargeMetricStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalEvChargeMetric("code-419", "title-369")
        item.status = RegionalEvChargeMetricStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalEvChargeMetricStatus.IDLE) }
    }
}
