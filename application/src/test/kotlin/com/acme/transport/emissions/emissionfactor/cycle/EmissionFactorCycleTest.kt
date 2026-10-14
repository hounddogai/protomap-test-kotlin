package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorCycleTest {
    @Test
    fun `starts in open`() {
        val item = newEmissionFactorCycle("code-696", "name-33")
        assertEquals(EmissionFactorCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newEmissionFactorCycle("code-696", "name-33")
        try {
            item.moveTo(EmissionFactorCycleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorCycleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEmissionFactorCycle("code-696", "name-33")
        item.status = EmissionFactorCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(EmissionFactorCycleStatus.OPEN) }
    }
}
