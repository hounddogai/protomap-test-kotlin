package com.acme.transport.customs.hold.holdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHoldAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newManualHoldAudit("code-955", "name-884")
        assertEquals(ManualHoldAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualHoldAudit("code-955", "name-884")
        try {
            item.moveTo(ManualHoldAuditStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHoldAuditStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualHoldAudit("code-955", "name-884")
        item.status = ManualHoldAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualHoldAuditStatus.PLANNED) }
    }
}
