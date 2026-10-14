package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelBurnBundle("code-641", "title-335")
        assertEquals(FuelBurnBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFuelBurnBundle("code-641", "title-335")
        try {
            item.moveTo(FuelBurnBundleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnBundleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelBurnBundle("code-641", "title-335")
        item.status = FuelBurnBundleStatus.CLOSED
        assertFails("move") { item.moveTo(FuelBurnBundleStatus.DRAFT) }
    }
}
