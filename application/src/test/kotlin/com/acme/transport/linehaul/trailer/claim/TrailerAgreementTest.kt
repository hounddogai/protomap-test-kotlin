package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerAgreement("code-559", "name-990")
        assertEquals(TrailerAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTrailerAgreement("code-559", "name-990")
        try {
            item.moveTo(TrailerAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerAgreement("code-559", "name-990")
        item.status = TrailerAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerAgreementStatus.REQUESTED) }
    }
}
