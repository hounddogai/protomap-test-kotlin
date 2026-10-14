package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputePlanTest {
    @Test
    fun `starts in idle`() {
        val item = newDisputePlan("code-856", "label-318")
        assertEquals(DisputePlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDisputePlan("code-856", "label-318")
        try {
            item.moveTo(DisputePlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputePlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDisputePlan("code-856", "label-318")
        item.status = DisputePlanStatus.STOPPED
        assertFails("move") { item.moveTo(DisputePlanStatus.IDLE) }
    }
}
