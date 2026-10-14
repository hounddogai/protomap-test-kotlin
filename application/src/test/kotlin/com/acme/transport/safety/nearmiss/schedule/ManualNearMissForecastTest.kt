package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualNearMissForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newManualNearMissForecast("code-933", "label-443")
        assertEquals(ManualNearMissForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualNearMissForecast("code-933", "label-443")
        try {
            item.moveTo(ManualNearMissForecastStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualNearMissForecastStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualNearMissForecast("code-933", "label-443")
        item.status = ManualNearMissForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualNearMissForecastStatus.REQUESTED) }
    }
}
