package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newEmissionFactorScore("code-988", "title-867")
        assertEquals(EmissionFactorScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newEmissionFactorScore("code-988", "title-867")
        try {
            item.moveTo(EmissionFactorScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEmissionFactorScore("code-988", "title-867")
        item.status = EmissionFactorScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(EmissionFactorScoreStatus.REQUESTED) }
    }
}
