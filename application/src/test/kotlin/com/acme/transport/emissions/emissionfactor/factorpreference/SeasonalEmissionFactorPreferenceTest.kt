package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEmissionFactorPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalEmissionFactorPreference("code-275", "title-669")
        assertEquals(SeasonalEmissionFactorPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSeasonalEmissionFactorPreference("code-275", "title-669")
        try {
            item.moveTo(SeasonalEmissionFactorPreferenceStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEmissionFactorPreferenceStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalEmissionFactorPreference("code-275", "title-669")
        item.status = SeasonalEmissionFactorPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalEmissionFactorPreferenceStatus.IDLE) }
    }
}
