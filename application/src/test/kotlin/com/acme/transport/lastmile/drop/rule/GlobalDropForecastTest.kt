package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDropForecastTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalDropForecast("code-294", "title-285")
        assertEquals(GlobalDropForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newGlobalDropForecast("code-294", "title-285")
        try {
            item.moveTo(GlobalDropForecastStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDropForecastStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalDropForecast("code-294", "title-285")
        item.status = GlobalDropForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalDropForecastStatus.OPEN) }
    }
}
