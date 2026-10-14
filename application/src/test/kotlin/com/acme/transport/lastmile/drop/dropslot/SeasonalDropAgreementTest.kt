package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDropAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalDropAgreement("code-430", "label-212")
        assertEquals(SeasonalDropAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalDropAgreement("code-430", "label-212")
        try {
            item.moveTo(SeasonalDropAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDropAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalDropAgreement("code-430", "label-212")
        item.status = SeasonalDropAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalDropAgreementStatus.DRAFT) }
    }
}
