package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAuditSegmentTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedAuditSegment("code-246", "title-581")
        assertEquals(WeightedAuditSegmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedAuditSegment("code-246", "title-581")
        try {
            item.moveTo(WeightedAuditSegmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAuditSegmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedAuditSegment("code-246", "title-581")
        item.status = WeightedAuditSegmentStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedAuditSegmentStatus.IDLE) }
    }
}
