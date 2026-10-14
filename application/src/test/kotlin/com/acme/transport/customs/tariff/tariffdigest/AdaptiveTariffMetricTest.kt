package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTariffMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        assertEquals(AdaptiveTariffMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        try {
            item.moveTo(AdaptiveTariffMetricStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTariffMetricStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        item.status = AdaptiveTariffMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveTariffMetricStatus.REQUESTED) }
    }
}
