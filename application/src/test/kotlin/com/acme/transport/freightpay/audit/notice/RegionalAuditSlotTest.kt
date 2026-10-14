package com.acme.transport.freightpay.audit.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalAuditSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalAuditSlot("code-424", "title-700")
        assertEquals(RegionalAuditSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalAuditSlot("code-424", "title-700")
        try {
            item.moveTo(RegionalAuditSlotStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalAuditSlotStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalAuditSlot("code-424", "title-700")
        item.status = RegionalAuditSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalAuditSlotStatus.REQUESTED) }
    }
}
