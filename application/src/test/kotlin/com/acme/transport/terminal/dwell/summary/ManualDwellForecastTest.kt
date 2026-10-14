package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellForecastTest {
    @Test
    fun `starts in created`() {
        val item = newManualDwellForecast("code-500", "title-369")
        assertEquals(ManualDwellForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualDwellForecast("code-500", "title-369")
        try {
            item.moveTo(ManualDwellForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualDwellForecast("code-500", "title-369")
        item.status = ManualDwellForecastStatus.RETIRED
        assertFails("move") { item.moveTo(ManualDwellForecastStatus.CREATED) }
    }
}
