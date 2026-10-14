package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireForecast("code-218", "label-81")
        assertEquals(TireForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTireForecast("code-218", "label-81")
        try {
            item.moveTo(TireForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireForecast("code-218", "label-81")
        item.status = TireForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireForecastStatus.PROPOSED) }
    }
}
