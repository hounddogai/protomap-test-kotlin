package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnTargetTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        assertEquals(RegionalFuelBurnTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        try {
            item.moveTo(RegionalFuelBurnTargetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnTargetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        item.status = RegionalFuelBurnTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalFuelBurnTargetStatus.OPEN) }
    }
}
