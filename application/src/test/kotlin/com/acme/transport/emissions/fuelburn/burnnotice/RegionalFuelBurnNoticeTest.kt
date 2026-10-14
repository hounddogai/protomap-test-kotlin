package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalFuelBurnNotice("code-658", "name-168")
        assertEquals(RegionalFuelBurnNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRegionalFuelBurnNotice("code-658", "name-168")
        try {
            item.moveTo(RegionalFuelBurnNoticeStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnNoticeStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalFuelBurnNotice("code-658", "name-168")
        item.status = RegionalFuelBurnNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalFuelBurnNoticeStatus.IDLE) }
    }
}
