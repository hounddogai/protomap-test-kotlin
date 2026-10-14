package com.acme.transport.customs.hold.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalHoldAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalHoldAudit("code-39", "title-777")
        assertEquals(SeasonalHoldAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalHoldAudit("code-39", "title-777")
        try {
            item.moveTo(SeasonalHoldAuditStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalHoldAuditStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalHoldAudit("code-39", "title-777")
        item.status = SeasonalHoldAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalHoldAuditStatus.REQUESTED) }
    }
}
