package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDropAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalDropAudit("code-307", "label-128")
        assertEquals(SeasonalDropAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalDropAudit("code-307", "label-128")
        try {
            item.moveTo(SeasonalDropAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDropAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalDropAudit("code-307", "label-128")
        item.status = SeasonalDropAuditStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalDropAuditStatus.IDLE) }
    }
}
