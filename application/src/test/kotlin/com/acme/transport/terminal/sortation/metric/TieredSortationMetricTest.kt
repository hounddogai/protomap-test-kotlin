package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredSortationMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredSortationMetric("code-859", "title-608")
        assertEquals(TieredSortationMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredSortationMetric("code-859", "title-608")
        try {
            item.moveTo(TieredSortationMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredSortationMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredSortationMetric("code-859", "title-608")
        item.status = TieredSortationMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredSortationMetricStatus.REQUESTED) }
    }
}
