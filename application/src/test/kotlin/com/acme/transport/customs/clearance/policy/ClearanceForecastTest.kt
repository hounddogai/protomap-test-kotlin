package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceForecastTest {
    @Test
    fun `starts in new`() {
        val item = newClearanceForecast("code-475", "title-276")
        assertEquals(ClearanceForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newClearanceForecast("code-475", "title-276")
        try {
            item.moveTo(ClearanceForecastStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceForecastStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newClearanceForecast("code-475", "title-276")
        item.status = ClearanceForecastStatus.FAILED
        assertFails("move") { item.moveTo(ClearanceForecastStatus.NEW) }
    }
}
