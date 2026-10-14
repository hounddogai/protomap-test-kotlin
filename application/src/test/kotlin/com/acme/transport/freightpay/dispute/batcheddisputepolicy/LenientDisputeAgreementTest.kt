package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDisputeAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newLenientDisputeAgreement("code-18", "name-735")
        assertEquals(LenientDisputeAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientDisputeAgreement("code-18", "name-735")
        try {
            item.moveTo(LenientDisputeAgreementStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDisputeAgreementStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientDisputeAgreement("code-18", "name-735")
        item.status = LenientDisputeAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(LenientDisputeAgreementStatus.CREATED) }
    }
}
