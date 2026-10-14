package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDropForecastTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledDropForecast("code-61", "name-647")
        assertEquals(ScheduledDropForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newScheduledDropForecast("code-61", "name-647")
        try {
            item.moveTo(ScheduledDropForecastStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDropForecastStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledDropForecast("code-61", "name-647")
        item.status = ScheduledDropForecastStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledDropForecastStatus.NEW) }
    }
}
