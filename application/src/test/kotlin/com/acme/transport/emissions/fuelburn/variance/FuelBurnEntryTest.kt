package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelBurnEntry("code-376", "label-644")
        assertEquals(FuelBurnEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFuelBurnEntry("code-376", "label-644")
        try {
            item.moveTo(FuelBurnEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelBurnEntry("code-376", "label-644")
        item.status = FuelBurnEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelBurnEntryStatus.REQUESTED) }
    }
}
