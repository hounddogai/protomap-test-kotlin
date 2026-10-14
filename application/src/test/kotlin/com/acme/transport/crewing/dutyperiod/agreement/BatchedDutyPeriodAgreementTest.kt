package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDutyPeriodAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedDutyPeriodAgreement("code-358", "title-999")
        assertEquals(BatchedDutyPeriodAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedDutyPeriodAgreement("code-358", "title-999")
        try {
            item.moveTo(BatchedDutyPeriodAgreementStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDutyPeriodAgreementStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedDutyPeriodAgreement("code-358", "title-999")
        item.status = BatchedDutyPeriodAgreementStatus.FAILED
        assertFails("move") { item.moveTo(BatchedDutyPeriodAgreementStatus.NEW) }
    }
}
