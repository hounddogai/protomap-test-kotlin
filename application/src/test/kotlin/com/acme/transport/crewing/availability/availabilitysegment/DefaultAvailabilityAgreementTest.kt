package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAvailabilityAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultAvailabilityAgreement("code-873", "label-730")
        assertEquals(DefaultAvailabilityAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultAvailabilityAgreement("code-873", "label-730")
        try {
            item.moveTo(DefaultAvailabilityAgreementStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAvailabilityAgreementStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultAvailabilityAgreement("code-873", "label-730")
        item.status = DefaultAvailabilityAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultAvailabilityAgreementStatus.IDLE) }
    }
}
