package com.acme.transport.emissions.fuelburn.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFuelBurnGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyFuelBurnGroup("code-308", "title-778")
        assertEquals(LegacyFuelBurnGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLegacyFuelBurnGroup("code-308", "title-778")
        try {
            item.moveTo(LegacyFuelBurnGroupStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFuelBurnGroupStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyFuelBurnGroup("code-308", "title-778")
        item.status = LegacyFuelBurnGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyFuelBurnGroupStatus.REQUESTED) }
    }
}
