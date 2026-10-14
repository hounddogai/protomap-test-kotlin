package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTireAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveTireAgreement("code-196", "title-642")
        assertEquals(AdaptiveTireAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveTireAgreement("code-196", "title-642")
        try {
            item.moveTo(AdaptiveTireAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTireAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveTireAgreement("code-196", "title-642")
        item.status = AdaptiveTireAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveTireAgreementStatus.DRAFT) }
    }
}
