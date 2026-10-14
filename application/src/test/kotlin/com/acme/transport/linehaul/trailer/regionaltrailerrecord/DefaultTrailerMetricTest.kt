package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTrailerMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultTrailerMetric("code-817", "name-457")
        assertEquals(DefaultTrailerMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultTrailerMetric("code-817", "name-457")
        try {
            item.moveTo(DefaultTrailerMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTrailerMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultTrailerMetric("code-817", "name-457")
        item.status = DefaultTrailerMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultTrailerMetricStatus.REQUESTED) }
    }
}
