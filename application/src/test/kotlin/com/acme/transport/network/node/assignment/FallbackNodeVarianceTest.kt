package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNodeVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackNodeVariance("code-685", "label-189")
        assertEquals(FallbackNodeVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackNodeVariance("code-685", "label-189")
        try {
            item.moveTo(FallbackNodeVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNodeVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackNodeVariance("code-685", "label-189")
        item.status = FallbackNodeVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackNodeVarianceStatus.PLANNED) }
    }
}
