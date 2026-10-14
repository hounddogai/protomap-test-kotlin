package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualViolationForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newManualViolationForecast("code-797", "label-911")
        assertEquals(ManualViolationForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualViolationForecast("code-797", "label-911")
        try {
            item.moveTo(ManualViolationForecastStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualViolationForecastStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualViolationForecast("code-797", "label-911")
        item.status = ManualViolationForecastStatus.STOPPED
        assertFails("move") { item.moveTo(ManualViolationForecastStatus.IDLE) }
    }
}
