package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorRollup("code-476", "title-232")
        assertEquals(EmissionFactorRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEmissionFactorRollup("code-476", "title-232")
        try {
            item.moveTo(EmissionFactorRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorRollup("code-476", "title-232")
        item.status = EmissionFactorRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorRollupStatus.REQUESTED) }
    }
}
