package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnProfileTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnProfile("code-672", "label-486")
        assertEquals(FuelBurnProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelBurnProfile("code-672", "label-486")
        try {
            item.moveTo(FuelBurnProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnProfile("code-672", "label-486")
        item.status = FuelBurnProfileStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnProfileStatus.CREATED) }
    }
}
