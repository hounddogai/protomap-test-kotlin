package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnTargetTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnTarget("code-813", "name-659")
        assertEquals(FuelBurnTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelBurnTarget("code-813", "name-659")
        try {
            item.moveTo(FuelBurnTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnTarget("code-813", "name-659")
        item.status = FuelBurnTargetStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnTargetStatus.CREATED) }
    }
}
