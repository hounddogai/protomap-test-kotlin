package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newAvailabilityAgreement("code-709", "label-133")
        assertEquals(AvailabilityAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newAvailabilityAgreement("code-709", "label-133")
        try {
            item.moveTo(AvailabilityAgreementStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityAgreementStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAvailabilityAgreement("code-709", "label-133")
        item.status = AvailabilityAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AvailabilityAgreementStatus.PROPOSED) }
    }
}
