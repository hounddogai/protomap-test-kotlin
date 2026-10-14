package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRosterAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultRosterAgreement("code-404", "label-177")
        assertEquals(DefaultRosterAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultRosterAgreement("code-404", "label-177")
        try {
            item.moveTo(DefaultRosterAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRosterAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultRosterAgreement("code-404", "label-177")
        item.status = DefaultRosterAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultRosterAgreementStatus.DRAFT) }
    }
}
