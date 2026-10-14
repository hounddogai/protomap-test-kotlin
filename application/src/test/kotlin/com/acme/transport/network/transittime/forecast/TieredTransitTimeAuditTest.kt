package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeAuditTest {
    @Test
    fun `starts in created`() {
        val item = newTieredTransitTimeAudit("code-50", "title-7")
        assertEquals(TieredTransitTimeAuditStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newTieredTransitTimeAudit("code-50", "title-7")
        try {
            item.moveTo(TieredTransitTimeAuditStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeAuditStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredTransitTimeAudit("code-50", "title-7")
        item.status = TieredTransitTimeAuditStatus.RETIRED
        assertFails("move") { item.moveTo(TieredTransitTimeAuditStatus.CREATED) }
    }
}
