package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEmissionFactorVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveEmissionFactorVariance("code-124", "label-278")
        assertEquals(AdaptiveEmissionFactorVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAdaptiveEmissionFactorVariance("code-124", "label-278")
        try {
            item.moveTo(AdaptiveEmissionFactorVarianceStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEmissionFactorVarianceStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveEmissionFactorVariance("code-124", "label-278")
        item.status = AdaptiveEmissionFactorVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveEmissionFactorVarianceStatus.IDLE) }
    }
}
