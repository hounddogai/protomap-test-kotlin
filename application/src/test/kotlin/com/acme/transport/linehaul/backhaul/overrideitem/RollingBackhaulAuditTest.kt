package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulAuditTest {
    @Test
    fun `starts in new`() {
        val item = newRollingBackhaulAudit("code-60", "label-856")
        assertEquals(RollingBackhaulAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingBackhaulAudit("code-60", "label-856")
        try {
            item.moveTo(RollingBackhaulAuditStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulAuditStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingBackhaulAudit("code-60", "label-856")
        item.status = RollingBackhaulAuditStatus.FAILED
        assertFails("move") { item.moveTo(RollingBackhaulAuditStatus.NEW) }
    }
}
