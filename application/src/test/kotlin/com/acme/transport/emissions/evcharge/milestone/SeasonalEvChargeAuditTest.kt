package com.acme.transport.emissions.evcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalEvChargeAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalEvChargeAudit("code-752", "label-603")
        assertEquals(SeasonalEvChargeAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalEvChargeAudit("code-752", "label-603")
        try {
            item.moveTo(SeasonalEvChargeAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalEvChargeAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalEvChargeAudit("code-752", "label-603")
        item.status = SeasonalEvChargeAuditStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalEvChargeAuditStatus.IDLE) }
    }
}
