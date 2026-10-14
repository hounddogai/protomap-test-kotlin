package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newDisputeAgreement("code-727", "title-140")
        assertEquals(DisputeAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDisputeAgreement("code-727", "title-140")
        try {
            item.moveTo(DisputeAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDisputeAgreement("code-727", "title-140")
        item.status = DisputeAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(DisputeAgreementStatus.CREATED) }
    }
}
