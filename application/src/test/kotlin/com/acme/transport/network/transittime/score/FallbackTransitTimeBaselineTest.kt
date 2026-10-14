package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackTransitTimeBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackTransitTimeBaseline("code-814", "name-397")
        assertEquals(FallbackTransitTimeBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackTransitTimeBaseline("code-814", "name-397")
        try {
            item.moveTo(FallbackTransitTimeBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackTransitTimeBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackTransitTimeBaseline("code-814", "name-397")
        item.status = FallbackTransitTimeBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackTransitTimeBaselineStatus.REQUESTED) }
    }
}
