package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAuditRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyAuditRecord("code-602", "label-915")
        assertEquals(LegacyAuditRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLegacyAuditRecord("code-602", "label-915")
        try {
            item.moveTo(LegacyAuditRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAuditRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyAuditRecord("code-602", "label-915")
        item.status = LegacyAuditRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyAuditRecordStatus.REQUESTED) }
    }
}
