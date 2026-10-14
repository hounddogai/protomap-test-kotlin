package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelBurnScore("code-754", "label-355")
        assertEquals(FuelBurnScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFuelBurnScore("code-754", "label-355")
        try {
            item.moveTo(FuelBurnScoreStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnScoreStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelBurnScore("code-754", "label-355")
        item.status = FuelBurnScoreStatus.STOPPED
        assertFails("move") { item.moveTo(FuelBurnScoreStatus.IDLE) }
    }
}
