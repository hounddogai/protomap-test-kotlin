package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newEmissionFactorMetric("code-845", "label-41")
        assertEquals(EmissionFactorMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newEmissionFactorMetric("code-845", "label-41")
        try {
            item.moveTo(EmissionFactorMetricStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorMetricStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newEmissionFactorMetric("code-845", "label-41")
        item.status = EmissionFactorMetricStatus.CLOSED
        assertFails("move") { item.moveTo(EmissionFactorMetricStatus.DRAFT) }
    }
}
