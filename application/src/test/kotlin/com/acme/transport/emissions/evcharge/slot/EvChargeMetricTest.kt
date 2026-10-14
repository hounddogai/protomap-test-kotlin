package com.acme.transport.emissions.evcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newEvChargeMetric("code-918", "label-773")
        assertEquals(EvChargeMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newEvChargeMetric("code-918", "label-773")
        try {
            item.moveTo(EvChargeMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newEvChargeMetric("code-918", "label-773")
        item.status = EvChargeMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(EvChargeMetricStatus.PROPOSED) }
    }
}
