package com.acme.transport.lastmile.drop.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newDropForecast("code-390", "title-962")
        assertEquals(DropForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDropForecast("code-390", "title-962")
        try {
            item.moveTo(DropForecastStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropForecastStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropForecast("code-390", "title-962")
        item.status = DropForecastStatus.STOPPED
        assertFails("move") { item.moveTo(DropForecastStatus.IDLE) }
    }
}
