package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingTireAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingTireAgreement("code-225", "title-127")
        assertEquals(PendingTireAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPendingTireAgreement("code-225", "title-127")
        try {
            item.moveTo(PendingTireAgreementStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingTireAgreementStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingTireAgreement("code-225", "title-127")
        item.status = PendingTireAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(PendingTireAgreementStatus.DRAFT) }
    }
}
