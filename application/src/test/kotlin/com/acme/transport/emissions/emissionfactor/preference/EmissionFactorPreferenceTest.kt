package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newEmissionFactorPreference("code-881", "label-440")
        assertEquals(EmissionFactorPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEmissionFactorPreference("code-881", "label-440")
        try {
            item.moveTo(EmissionFactorPreferenceStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorPreferenceStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEmissionFactorPreference("code-881", "label-440")
        item.status = EmissionFactorPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(EmissionFactorPreferenceStatus.NEW) }
    }
}
