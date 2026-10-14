package com.acme.transport.crewing.restbreak.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRestBreakOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackRestBreakOverride("code-275", "label-722")
        assertEquals(FallbackRestBreakOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackRestBreakOverride("code-275", "label-722")
        try {
            item.moveTo(FallbackRestBreakOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRestBreakOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackRestBreakOverride("code-275", "label-722")
        item.status = FallbackRestBreakOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackRestBreakOverrideStatus.PLANNED) }
    }
}
