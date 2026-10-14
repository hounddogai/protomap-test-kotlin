package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyTireThreshold("code-610", "label-539")
        assertEquals(LegacyTireThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLegacyTireThreshold("code-610", "label-539")
        try {
            item.moveTo(LegacyTireThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyTireThreshold("code-610", "label-539")
        item.status = LegacyTireThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyTireThresholdStatus.PROPOSED) }
    }
}
