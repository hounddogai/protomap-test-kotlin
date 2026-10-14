package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZoneForecastTest {
    @Test
    fun `starts in created`() {
        val item = newTieredZoneForecast("code-966", "label-590")
        assertEquals(TieredZoneForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredZoneForecast("code-966", "label-590")
        try {
            item.moveTo(TieredZoneForecastStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZoneForecastStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredZoneForecast("code-966", "label-590")
        item.status = TieredZoneForecastStatus.RETIRED
        assertFails("move") { item.moveTo(TieredZoneForecastStatus.CREATED) }
    }
}
