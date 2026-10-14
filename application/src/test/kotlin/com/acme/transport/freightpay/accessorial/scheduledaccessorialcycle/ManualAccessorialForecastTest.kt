package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAccessorialForecastTest {
    @Test
    fun `starts in open`() {
        val item = newManualAccessorialForecast("code-886", "label-330")
        assertEquals(ManualAccessorialForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualAccessorialForecast("code-886", "label-330")
        try {
            item.moveTo(ManualAccessorialForecastStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAccessorialForecastStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualAccessorialForecast("code-886", "label-330")
        item.status = ManualAccessorialForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualAccessorialForecastStatus.OPEN) }
    }
}
