package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderAuditTest {
    @Test
    fun `starts in new`() {
        val item = newTenderAudit("code-442", "title-397")
        assertEquals(TenderAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTenderAudit("code-442", "title-397")
        try {
            item.moveTo(TenderAuditStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderAuditStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTenderAudit("code-442", "title-397")
        item.status = TenderAuditStatus.FAILED
        assertFails("move") { item.moveTo(TenderAuditStatus.NEW) }
    }
}
