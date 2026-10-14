package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerYardAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        assertEquals(ManualTrailerYardAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        try {
            item.moveTo(ManualTrailerYardAdjustmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerYardAdjustmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        item.status = ManualTrailerYardAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualTrailerYardAdjustmentStatus.PROPOSED) }
    }
}
