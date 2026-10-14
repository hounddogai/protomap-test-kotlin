package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelBurnOverride("code-571", "name-741")
        assertEquals(FuelBurnOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFuelBurnOverride("code-571", "name-741")
        try {
            item.moveTo(FuelBurnOverrideStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnOverrideStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelBurnOverride("code-571", "name-741")
        item.status = FuelBurnOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(FuelBurnOverrideStatus.DRAFT) }
    }
}
