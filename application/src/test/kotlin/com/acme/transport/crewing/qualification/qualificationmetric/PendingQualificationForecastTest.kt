package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingQualificationForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingQualificationForecast("code-997", "title-646")
        assertEquals(PendingQualificationForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingQualificationForecast("code-997", "title-646")
        try {
            item.moveTo(PendingQualificationForecastStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingQualificationForecastStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingQualificationForecast("code-997", "title-646")
        item.status = PendingQualificationForecastStatus.STOPPED
        assertFails("move") { item.moveTo(PendingQualificationForecastStatus.IDLE) }
    }
}
