package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedRosterAgreement("code-868", "title-218")
        assertEquals(BatchedRosterAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedRosterAgreement("code-868", "title-218")
        try {
            item.moveTo(BatchedRosterAgreementStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterAgreementStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedRosterAgreement("code-868", "title-218")
        item.status = BatchedRosterAgreementStatus.FAILED
        assertFails("move") { item.moveTo(BatchedRosterAgreementStatus.NEW) }
    }
}
