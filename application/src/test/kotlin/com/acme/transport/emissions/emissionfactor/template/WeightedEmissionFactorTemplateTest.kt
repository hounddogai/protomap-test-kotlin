package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEmissionFactorTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedEmissionFactorTemplate("code-867", "name-76")
        assertEquals(WeightedEmissionFactorTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedEmissionFactorTemplate("code-867", "name-76")
        try {
            item.moveTo(WeightedEmissionFactorTemplateStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEmissionFactorTemplateStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedEmissionFactorTemplate("code-867", "name-76")
        item.status = WeightedEmissionFactorTemplateStatus.FAILED
        assertFails("move") { item.moveTo(WeightedEmissionFactorTemplateStatus.NEW) }
    }
}
