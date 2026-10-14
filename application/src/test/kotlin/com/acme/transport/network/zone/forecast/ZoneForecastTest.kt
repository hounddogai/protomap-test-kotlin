package com.acme.transport.network.zone.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newZoneForecast("code-156", "label-18")
        assertEquals(ZoneForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newZoneForecast("code-156", "label-18")
        try {
            item.moveTo(ZoneForecastStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneForecastStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newZoneForecast("code-156", "label-18")
        item.status = ZoneForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(ZoneForecastStatus.REQUESTED) }
    }
}
