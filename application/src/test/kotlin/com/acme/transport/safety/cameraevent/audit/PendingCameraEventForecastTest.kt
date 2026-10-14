package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCameraEventForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingCameraEventForecast("code-800", "title-432")
        assertEquals(PendingCameraEventForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingCameraEventForecast("code-800", "title-432")
        try {
            item.moveTo(PendingCameraEventForecastStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCameraEventForecastStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingCameraEventForecast("code-800", "title-432")
        item.status = PendingCameraEventForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingCameraEventForecastStatus.REQUESTED) }
    }
}
