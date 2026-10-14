package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanAudit("code-446", "label-43")
        assertEquals(LanePlanAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newLanePlanAudit("code-446", "label-43")
        try {
            item.moveTo(LanePlanAuditStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanAuditStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanAudit("code-446", "label-43")
        item.status = LanePlanAuditStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanAuditStatus.IDLE) }
    }
}
