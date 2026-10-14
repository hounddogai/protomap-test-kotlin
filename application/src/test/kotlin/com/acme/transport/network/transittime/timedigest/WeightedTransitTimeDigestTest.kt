package com.acme.transport.network.transittime.timedigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTransitTimeDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTransitTimeDigest("code-733", "title-151")
        assertEquals(WeightedTransitTimeDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedTransitTimeDigest("code-733", "title-151")
        try {
            item.moveTo(WeightedTransitTimeDigestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTransitTimeDigestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTransitTimeDigest("code-733", "title-151")
        item.status = WeightedTransitTimeDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTransitTimeDigestStatus.PLANNED) }
    }
}
