package com.acme.transport.fleetops.leasereturn.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newLeaseReturnAgreement("code-530", "title-71")
        assertEquals(LeaseReturnAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLeaseReturnAgreement("code-530", "title-71")
        try {
            item.moveTo(LeaseReturnAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLeaseReturnAgreement("code-530", "title-71")
        item.status = LeaseReturnAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(LeaseReturnAgreementStatus.IDLE) }
    }
}
