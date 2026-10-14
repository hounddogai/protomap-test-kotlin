package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictViolationForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictViolationForecast("code-163", "title-938")
        assertEquals(StrictViolationForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictViolationForecast("code-163", "title-938")
        try {
            item.moveTo(StrictViolationForecastStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictViolationForecastStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictViolationForecast("code-163", "title-938")
        item.status = StrictViolationForecastStatus.CLOSED
        assertFails("move") { item.moveTo(StrictViolationForecastStatus.DRAFT) }
    }
}
