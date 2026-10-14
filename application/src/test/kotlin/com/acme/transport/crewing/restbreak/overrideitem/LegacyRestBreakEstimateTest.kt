package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRestBreakEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyRestBreakEstimate("code-203", "title-533")
        assertEquals(LegacyRestBreakEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyRestBreakEstimate("code-203", "title-533")
        try {
            item.moveTo(LegacyRestBreakEstimateStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRestBreakEstimateStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyRestBreakEstimate("code-203", "title-533")
        item.status = LegacyRestBreakEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyRestBreakEstimateStatus.IDLE) }
    }
}
