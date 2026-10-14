package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellForecastTest {
    @Test
    fun `starts in created`() {
        val item = newLenientDwellForecast("code-628", "name-503")
        assertEquals(LenientDwellForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientDwellForecast("code-628", "name-503")
        try {
            item.moveTo(LenientDwellForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientDwellForecast("code-628", "name-503")
        item.status = LenientDwellForecastStatus.RETIRED
        assertFails("move") { item.moveTo(LenientDwellForecastStatus.CREATED) }
    }
}
