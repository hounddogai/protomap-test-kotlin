package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newDisputeRecord("code-192", "label-763")
        assertEquals(DisputeRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDisputeRecord("code-192", "label-763")
        try {
            item.moveTo(DisputeRecordStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeRecordStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDisputeRecord("code-192", "label-763")
        item.status = DisputeRecordStatus.STOPPED
        assertFails("move") { item.moveTo(DisputeRecordStatus.IDLE) }
    }
}
