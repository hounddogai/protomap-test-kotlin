package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputePolicyTest {
    @Test
    fun `starts in open`() {
        val item = newDisputePolicy("code-397", "label-957")
        assertEquals(DisputePolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDisputePolicy("code-397", "label-957")
        try {
            item.moveTo(DisputePolicyStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputePolicyStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputePolicy("code-397", "label-957")
        item.status = DisputePolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputePolicyStatus.OPEN) }
    }
}
