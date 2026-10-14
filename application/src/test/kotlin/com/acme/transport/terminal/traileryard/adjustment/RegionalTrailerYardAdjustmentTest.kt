package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerYardAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalTrailerYardAdjustment("code-882", "label-488")
        assertEquals(RegionalTrailerYardAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRegionalTrailerYardAdjustment("code-882", "label-488")
        try {
            item.moveTo(RegionalTrailerYardAdjustmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerYardAdjustmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalTrailerYardAdjustment("code-882", "label-488")
        item.status = RegionalTrailerYardAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalTrailerYardAdjustmentStatus.PROPOSED) }
    }
}
