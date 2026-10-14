package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEmissionFactorGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedEmissionFactorGroup("code-218", "name-45")
        assertEquals(WeightedEmissionFactorGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedEmissionFactorGroup("code-218", "name-45")
        try {
            item.moveTo(WeightedEmissionFactorGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEmissionFactorGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedEmissionFactorGroup("code-218", "name-45")
        item.status = WeightedEmissionFactorGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedEmissionFactorGroupStatus.REQUESTED) }
    }
}
