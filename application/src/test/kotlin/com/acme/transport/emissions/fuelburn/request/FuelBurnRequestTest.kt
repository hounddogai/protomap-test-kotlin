package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newFuelBurnRequest("code-875", "name-276")
        assertEquals(FuelBurnRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFuelBurnRequest("code-875", "name-276")
        try {
            item.moveTo(FuelBurnRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFuelBurnRequest("code-875", "name-276")
        item.status = FuelBurnRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FuelBurnRequestStatus.PROPOSED) }
    }
}
