package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterAgreement("code-515", "name-950")
        assertEquals(RosterAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRosterAgreement("code-515", "name-950")
        try {
            item.moveTo(RosterAgreementStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterAgreementStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterAgreement("code-515", "name-950")
        item.status = RosterAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterAgreementStatus.REQUESTED) }
    }
}
