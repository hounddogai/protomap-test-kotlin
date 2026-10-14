package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnDigestTest {
    @Test
    fun `starts in proposed`() {
        val item = newFuelBurnDigest("code-615", "name-919")
        assertEquals(FuelBurnDigestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFuelBurnDigest("code-615", "name-919")
        try {
            item.moveTo(FuelBurnDigestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnDigestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFuelBurnDigest("code-615", "name-919")
        item.status = FuelBurnDigestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FuelBurnDigestStatus.PROPOSED) }
    }
}
