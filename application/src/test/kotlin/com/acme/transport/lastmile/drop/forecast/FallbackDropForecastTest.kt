package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDropForecastTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDropForecast("code-693", "title-736")
        assertEquals(FallbackDropForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFallbackDropForecast("code-693", "title-736")
        try {
            item.moveTo(FallbackDropForecastStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDropForecastStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDropForecast("code-693", "title-736")
        item.status = FallbackDropForecastStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDropForecastStatus.NEW) }
    }
}
