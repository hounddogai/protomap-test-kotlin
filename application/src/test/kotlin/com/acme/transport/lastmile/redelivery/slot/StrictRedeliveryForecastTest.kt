package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRedeliveryForecastTest {
    @Test
    fun `starts in open`() {
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        assertEquals(StrictRedeliveryForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        try {
            item.moveTo(StrictRedeliveryForecastStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRedeliveryForecastStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictRedeliveryForecast("code-367", "label-688")
        item.status = StrictRedeliveryForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictRedeliveryForecastStatus.OPEN) }
    }
}
