package com.acme.transport.terminal.dockdoor.doorestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDockDoorForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalDockDoorForecast("code-670", "label-820")
        assertEquals(RegionalDockDoorForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRegionalDockDoorForecast("code-670", "label-820")
        try {
            item.moveTo(RegionalDockDoorForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDockDoorForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalDockDoorForecast("code-670", "label-820")
        item.status = RegionalDockDoorForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalDockDoorForecastStatus.PLANNED) }
    }
}
