package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newDockDoorForecast("code-771", "name-30")
        assertEquals(DockDoorForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDockDoorForecast("code-771", "name-30")
        try {
            item.moveTo(DockDoorForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDockDoorForecast("code-771", "name-30")
        item.status = DockDoorForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(DockDoorForecastStatus.PLANNED) }
    }
}
