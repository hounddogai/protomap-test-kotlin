package com.acme.transport.linehaul.backhaul.backhaultarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBackhaulForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingBackhaulForecast("code-363", "title-811")
        assertEquals(PendingBackhaulForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newPendingBackhaulForecast("code-363", "title-811")
        try {
            item.moveTo(PendingBackhaulForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBackhaulForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingBackhaulForecast("code-363", "title-811")
        item.status = PendingBackhaulForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingBackhaulForecastStatus.PROPOSED) }
    }
}
