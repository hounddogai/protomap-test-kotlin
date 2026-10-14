package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalEmissionFactorCycle("code-229", "label-172")
        assertEquals(SeasonalEmissionFactorCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalEmissionFactorCycle("code-229", "label-172")
        try {
            item.moveTo(SeasonalEmissionFactorCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalEmissionFactorCycle("code-229", "label-172")
        item.status = SeasonalEmissionFactorCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalEmissionFactorCycleStatus.PROPOSED) }
    }
}
