package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityForecast("code-230", "name-82")
        assertEquals(CapacityForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCapacityForecast("code-230", "name-82")
        try {
            item.moveTo(CapacityForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityForecast("code-230", "name-82")
        item.status = CapacityForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityForecastStatus.PROPOSED) }
    }
}
