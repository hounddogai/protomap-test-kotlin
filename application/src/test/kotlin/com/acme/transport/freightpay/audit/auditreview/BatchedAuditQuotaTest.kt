package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAuditQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedAuditQuota("code-157", "label-190")
        assertEquals(BatchedAuditQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBatchedAuditQuota("code-157", "label-190")
        try {
            item.moveTo(BatchedAuditQuotaStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAuditQuotaStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedAuditQuota("code-157", "label-190")
        item.status = BatchedAuditQuotaStatus.FAILED
        assertFails("move") { item.moveTo(BatchedAuditQuotaStatus.NEW) }
    }
}
