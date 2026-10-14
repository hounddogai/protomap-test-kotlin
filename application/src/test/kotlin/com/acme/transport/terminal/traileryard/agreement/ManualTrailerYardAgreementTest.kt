package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerYardAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newManualTrailerYardAgreement("code-196", "name-197")
        assertEquals(ManualTrailerYardAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newManualTrailerYardAgreement("code-196", "name-197")
        try {
            item.moveTo(ManualTrailerYardAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerYardAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualTrailerYardAgreement("code-196", "name-197")
        item.status = ManualTrailerYardAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualTrailerYardAgreementStatus.PLANNED) }
    }
}
