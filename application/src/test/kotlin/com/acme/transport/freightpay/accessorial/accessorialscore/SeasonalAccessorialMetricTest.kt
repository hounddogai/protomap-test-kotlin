package com.acme.transport.freightpay.accessorial.accessorialscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialMetricTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        assertEquals(SeasonalAccessorialMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        try {
            item.moveTo(SeasonalAccessorialMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalAccessorialMetric("code-579", "title-804")
        item.status = SeasonalAccessorialMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalAccessorialMetricStatus.OPEN) }
    }
}
