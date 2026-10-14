package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerYardAgreement("code-385", "title-445")
        assertEquals(TrailerYardAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTrailerYardAgreement("code-385", "title-445")
        try {
            item.moveTo(TrailerYardAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerYardAgreement("code-385", "title-445")
        item.status = TrailerYardAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerYardAgreementStatus.PLANNED) }
    }
}
