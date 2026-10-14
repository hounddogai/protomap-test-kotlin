package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCapacityForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedCapacityForecast("code-198", "name-649")
        assertEquals(CappedCapacityForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedCapacityForecast("code-198", "name-649")
        try {
            item.moveTo(CappedCapacityForecastStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCapacityForecastStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedCapacityForecast("code-198", "name-649")
        item.status = CappedCapacityForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedCapacityForecastStatus.REQUESTED) }
    }
}
