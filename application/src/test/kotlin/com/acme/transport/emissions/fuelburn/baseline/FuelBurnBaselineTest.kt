package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelBurnBaseline("code-994", "name-228")
        assertEquals(FuelBurnBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFuelBurnBaseline("code-994", "name-228")
        try {
            item.moveTo(FuelBurnBaselineStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnBaselineStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelBurnBaseline("code-994", "name-228")
        item.status = FuelBurnBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelBurnBaselineStatus.REQUESTED) }
    }
}
