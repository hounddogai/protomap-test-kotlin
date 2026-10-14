package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFuelBurnMetricTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryFuelBurnMetric("code-283", "label-590")
        assertEquals(PrimaryFuelBurnMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryFuelBurnMetric("code-283", "label-590")
        try {
            item.moveTo(PrimaryFuelBurnMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFuelBurnMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryFuelBurnMetric("code-283", "label-590")
        item.status = PrimaryFuelBurnMetricStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryFuelBurnMetricStatus.CREATED) }
    }
}
