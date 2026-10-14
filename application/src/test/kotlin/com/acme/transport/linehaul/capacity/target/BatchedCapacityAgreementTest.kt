package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCapacityAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedCapacityAgreement("code-810", "title-81")
        assertEquals(BatchedCapacityAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedCapacityAgreement("code-810", "title-81")
        try {
            item.moveTo(BatchedCapacityAgreementStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCapacityAgreementStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedCapacityAgreement("code-810", "title-81")
        item.status = BatchedCapacityAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedCapacityAgreementStatus.CREATED) }
    }
}
