package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newAccessorialForecast("code-23", "title-434")
        assertEquals(AccessorialForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newAccessorialForecast("code-23", "title-434")
        try {
            item.moveTo(AccessorialForecastStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialForecastStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAccessorialForecast("code-23", "title-434")
        item.status = AccessorialForecastStatus.CLOSED
        assertFails("move") { item.moveTo(AccessorialForecastStatus.DRAFT) }
    }
}
