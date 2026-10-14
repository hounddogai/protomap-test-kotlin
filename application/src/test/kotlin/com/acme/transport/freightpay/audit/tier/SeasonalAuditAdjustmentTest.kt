package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAuditAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalAuditAdjustment("code-934", "label-327")
        assertEquals(SeasonalAuditAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalAuditAdjustment("code-934", "label-327")
        try {
            item.moveTo(SeasonalAuditAdjustmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAuditAdjustmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalAuditAdjustment("code-934", "label-327")
        item.status = SeasonalAuditAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalAuditAdjustmentStatus.CREATED) }
    }
}
