package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyEmissionFactorPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyEmissionFactorPreference("code-97", "title-444")
        assertEquals(LegacyEmissionFactorPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLegacyEmissionFactorPreference("code-97", "title-444")
        try {
            item.moveTo(LegacyEmissionFactorPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyEmissionFactorPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyEmissionFactorPreference("code-97", "title-444")
        item.status = LegacyEmissionFactorPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(LegacyEmissionFactorPreferenceStatus.NEW) }
    }
}
