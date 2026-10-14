package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationForecast("code-854", "label-1")
        assertEquals(ViolationForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationForecast("code-854", "label-1")
        try {
            item.moveTo(ViolationForecastStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationForecastStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationForecast("code-854", "label-1")
        item.status = ViolationForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationForecastStatus.REQUESTED) }
    }
}
