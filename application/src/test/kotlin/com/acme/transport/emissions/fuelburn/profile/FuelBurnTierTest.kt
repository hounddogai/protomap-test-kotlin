package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnTierTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnTier("code-477", "label-133")
        assertEquals(FuelBurnTierStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFuelBurnTier("code-477", "label-133")
        try {
            item.moveTo(FuelBurnTierStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnTierStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnTier("code-477", "label-133")
        item.status = FuelBurnTierStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnTierStatus.CREATED) }
    }
}
