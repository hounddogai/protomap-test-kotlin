package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalFuelBurnScore("code-522", "name-201")
        assertEquals(RegionalFuelBurnScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalFuelBurnScore("code-522", "name-201")
        try {
            item.moveTo(RegionalFuelBurnScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalFuelBurnScore("code-522", "name-201")
        item.status = RegionalFuelBurnScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalFuelBurnScoreStatus.REQUESTED) }
    }
}
