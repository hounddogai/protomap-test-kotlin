package com.acme.transport.network.transittime.timethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTransitTimeThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackTransitTimeThreshold("code-445", "name-704")
        assertEquals(FallbackTransitTimeThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackTransitTimeThreshold("code-445", "name-704")
        try {
            item.moveTo(FallbackTransitTimeThresholdStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTransitTimeThresholdStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackTransitTimeThreshold("code-445", "name-704")
        item.status = FallbackTransitTimeThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackTransitTimeThresholdStatus.OPEN) }
    }
}
