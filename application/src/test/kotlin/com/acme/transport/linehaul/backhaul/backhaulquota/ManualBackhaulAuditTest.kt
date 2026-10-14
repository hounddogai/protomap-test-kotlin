package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBackhaulAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newManualBackhaulAudit("code-638", "label-451")
        assertEquals(ManualBackhaulAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualBackhaulAudit("code-638", "label-451")
        try {
            item.moveTo(ManualBackhaulAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBackhaulAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualBackhaulAudit("code-638", "label-451")
        item.status = ManualBackhaulAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualBackhaulAuditStatus.PLANNED) }
    }
}
