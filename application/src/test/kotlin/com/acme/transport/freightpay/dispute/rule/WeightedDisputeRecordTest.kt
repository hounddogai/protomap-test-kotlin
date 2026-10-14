package com.acme.transport.freightpay.dispute.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDisputeRecordTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedDisputeRecord("code-600", "label-638")
        assertEquals(WeightedDisputeRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newWeightedDisputeRecord("code-600", "label-638")
        try {
            item.moveTo(WeightedDisputeRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDisputeRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedDisputeRecord("code-600", "label-638")
        item.status = WeightedDisputeRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedDisputeRecordStatus.OPEN) }
    }
}
