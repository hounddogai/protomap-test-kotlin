package com.acme.transport.emissions.emissionfactor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEmissionFactorThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedEmissionFactorThreshold("code-958", "title-933")
        assertEquals(WeightedEmissionFactorThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedEmissionFactorThreshold("code-958", "title-933")
        try {
            item.moveTo(WeightedEmissionFactorThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEmissionFactorThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedEmissionFactorThreshold("code-958", "title-933")
        item.status = WeightedEmissionFactorThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedEmissionFactorThresholdStatus.CREATED) }
    }
}
