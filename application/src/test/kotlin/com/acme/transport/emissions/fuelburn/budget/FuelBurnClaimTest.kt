package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnClaimTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnClaim("code-291", "title-204")
        assertEquals(FuelBurnClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFuelBurnClaim("code-291", "title-204")
        try {
            item.moveTo(FuelBurnClaimStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnClaimStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnClaim("code-291", "title-204")
        item.status = FuelBurnClaimStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnClaimStatus.CREATED) }
    }
}
