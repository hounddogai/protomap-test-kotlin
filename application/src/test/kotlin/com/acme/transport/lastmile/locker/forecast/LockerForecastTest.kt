package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerForecastTest {
    @Test
    fun `starts in open`() {
        val item = newLockerForecast("code-224", "label-537")
        assertEquals(LockerForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLockerForecast("code-224", "label-537")
        try {
            item.moveTo(LockerForecastStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerForecastStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLockerForecast("code-224", "label-537")
        item.status = LockerForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(LockerForecastStatus.OPEN) }
    }
}
