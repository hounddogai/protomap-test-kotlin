package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditRecordTest {
    @Test
    fun `starts in created`() {
        val item = newAuditRecord("code-670", "label-664")
        assertEquals(AuditRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAuditRecord("code-670", "label-664")
        try {
            item.moveTo(AuditRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAuditRecord("code-670", "label-664")
        item.status = AuditRecordStatus.RETIRED
        assertFails("move") { item.moveTo(AuditRecordStatus.CREATED) }
    }
}
