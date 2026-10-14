package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newRedeliveryAgreement("code-219", "title-876")
        assertEquals(RedeliveryAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRedeliveryAgreement("code-219", "title-876")
        try {
            item.moveTo(RedeliveryAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRedeliveryAgreement("code-219", "title-876")
        item.status = RedeliveryAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(RedeliveryAgreementStatus.CREATED) }
    }
}
