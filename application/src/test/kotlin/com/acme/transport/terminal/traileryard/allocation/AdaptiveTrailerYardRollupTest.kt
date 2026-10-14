package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerYardRollupTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveTrailerYardRollup("code-433", "label-946")
        assertEquals(AdaptiveTrailerYardRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveTrailerYardRollup("code-433", "label-946")
        try {
            item.moveTo(AdaptiveTrailerYardRollupStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerYardRollupStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveTrailerYardRollup("code-433", "label-946")
        item.status = AdaptiveTrailerYardRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveTrailerYardRollupStatus.OPEN) }
    }
}
