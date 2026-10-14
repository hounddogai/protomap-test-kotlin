package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveEmissionFactorEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveEmissionFactorEntry("code-158", "title-713")
        assertEquals(AdaptiveEmissionFactorEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAdaptiveEmissionFactorEntry("code-158", "title-713")
        try {
            item.moveTo(AdaptiveEmissionFactorEntryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveEmissionFactorEntryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveEmissionFactorEntry("code-158", "title-713")
        item.status = AdaptiveEmissionFactorEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveEmissionFactorEntryStatus.PLANNED) }
    }
}
