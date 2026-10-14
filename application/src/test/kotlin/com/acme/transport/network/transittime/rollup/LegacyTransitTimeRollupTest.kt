package com.acme.transport.network.transittime.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTransitTimeRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyTransitTimeRollup("code-265", "label-126")
        assertEquals(LegacyTransitTimeRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyTransitTimeRollup("code-265", "label-126")
        try {
            item.moveTo(LegacyTransitTimeRollupStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTransitTimeRollupStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyTransitTimeRollup("code-265", "label-126")
        item.status = LegacyTransitTimeRollupStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyTransitTimeRollupStatus.IDLE) }
    }
}
