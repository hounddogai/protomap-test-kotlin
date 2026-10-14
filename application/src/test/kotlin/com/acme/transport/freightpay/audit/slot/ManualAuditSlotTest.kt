package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAuditSlotTest {
    @Test
    fun `starts in open`() {
        val item = newManualAuditSlot("code-909", "title-468")
        assertEquals(ManualAuditSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualAuditSlot("code-909", "title-468")
        try {
            item.moveTo(ManualAuditSlotStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAuditSlotStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualAuditSlot("code-909", "title-468")
        item.status = ManualAuditSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualAuditSlotStatus.OPEN) }
    }
}
