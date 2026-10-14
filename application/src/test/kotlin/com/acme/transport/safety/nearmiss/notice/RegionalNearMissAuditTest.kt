package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNearMissAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalNearMissAudit("code-889", "name-549")
        assertEquals(RegionalNearMissAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalNearMissAudit("code-889", "name-549")
        try {
            item.moveTo(RegionalNearMissAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNearMissAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalNearMissAudit("code-889", "name-549")
        item.status = RegionalNearMissAuditStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalNearMissAuditStatus.IDLE) }
    }
}
