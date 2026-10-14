package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedZoneAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedZoneAgreement("code-456", "label-377")
        assertEquals(BatchedZoneAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedZoneAgreement("code-456", "label-377")
        try {
            item.moveTo(BatchedZoneAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedZoneAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedZoneAgreement("code-456", "label-377")
        item.status = BatchedZoneAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedZoneAgreementStatus.DRAFT) }
    }
}
