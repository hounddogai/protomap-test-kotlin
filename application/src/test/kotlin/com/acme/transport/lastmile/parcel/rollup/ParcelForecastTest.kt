package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newParcelForecast("code-931", "name-745")
        assertEquals(ParcelForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newParcelForecast("code-931", "name-745")
        try {
            item.moveTo(ParcelForecastStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelForecastStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newParcelForecast("code-931", "name-745")
        item.status = ParcelForecastStatus.STOPPED
        assertFails("move") { item.moveTo(ParcelForecastStatus.IDLE) }
    }
}
