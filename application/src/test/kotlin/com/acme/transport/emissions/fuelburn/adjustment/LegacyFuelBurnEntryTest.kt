package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFuelBurnEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyFuelBurnEntry("code-16", "name-707")
        assertEquals(LegacyFuelBurnEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyFuelBurnEntry("code-16", "name-707")
        try {
            item.moveTo(LegacyFuelBurnEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFuelBurnEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyFuelBurnEntry("code-16", "name-707")
        item.status = LegacyFuelBurnEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyFuelBurnEntryStatus.REQUESTED) }
    }
}
