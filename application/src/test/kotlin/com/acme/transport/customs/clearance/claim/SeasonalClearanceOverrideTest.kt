package com.acme.transport.customs.clearance.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalClearanceOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalClearanceOverride("code-519", "title-555")
        assertEquals(SeasonalClearanceOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newSeasonalClearanceOverride("code-519", "title-555")
        try {
            item.moveTo(SeasonalClearanceOverrideStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalClearanceOverrideStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalClearanceOverride("code-519", "title-555")
        item.status = SeasonalClearanceOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalClearanceOverrideStatus.IDLE) }
    }
}
