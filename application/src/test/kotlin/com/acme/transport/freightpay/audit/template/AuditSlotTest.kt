package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditSlotTest {
    @Test
    fun `starts in open`() {
        val item = newAuditSlot("code-109", "label-677")
        assertEquals(AuditSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAuditSlot("code-109", "label-677")
        try {
            item.moveTo(AuditSlotStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditSlotStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAuditSlot("code-109", "label-677")
        item.status = AuditSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(AuditSlotStatus.OPEN) }
    }
}
