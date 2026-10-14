package com.acme.transport.freightpay.dispute.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newDisputeEstimate("code-892", "label-638")
        assertEquals(DisputeEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDisputeEstimate("code-892", "label-638")
        try {
            item.moveTo(DisputeEstimateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeEstimateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDisputeEstimate("code-892", "label-638")
        item.status = DisputeEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(DisputeEstimateStatus.CREATED) }
    }
}
