package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorForecast("code-434", "label-740")
        assertEquals(EmissionFactorForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newEmissionFactorForecast("code-434", "label-740")
        try {
            item.moveTo(EmissionFactorForecastStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorForecastStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorForecast("code-434", "label-740")
        item.status = EmissionFactorForecastStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorForecastStatus.IDLE) }
    }
}
