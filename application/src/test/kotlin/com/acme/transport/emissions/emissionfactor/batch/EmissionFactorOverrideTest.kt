package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newEmissionFactorOverride("code-259", "name-555")
        assertEquals(EmissionFactorOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newEmissionFactorOverride("code-259", "name-555")
        try {
            item.moveTo(EmissionFactorOverrideStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorOverrideStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newEmissionFactorOverride("code-259", "name-555")
        item.status = EmissionFactorOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(EmissionFactorOverrideStatus.PLANNED) }
    }
}
