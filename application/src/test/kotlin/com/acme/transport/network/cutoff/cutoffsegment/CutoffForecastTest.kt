package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffForecastTest {
    @Test
    fun `starts in new`() {
        val item = newCutoffForecast("code-679", "title-620")
        assertEquals(CutoffForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCutoffForecast("code-679", "title-620")
        try {
            item.moveTo(CutoffForecastStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffForecastStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCutoffForecast("code-679", "title-620")
        item.status = CutoffForecastStatus.FAILED
        assertFails("move") { item.moveTo(CutoffForecastStatus.NEW) }
    }
}
