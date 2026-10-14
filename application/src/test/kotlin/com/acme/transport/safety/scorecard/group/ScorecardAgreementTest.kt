package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newScorecardAgreement("code-705", "name-179")
        assertEquals(ScorecardAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScorecardAgreement("code-705", "name-179")
        try {
            item.moveTo(ScorecardAgreementStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardAgreementStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScorecardAgreement("code-705", "name-179")
        item.status = ScorecardAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(ScorecardAgreementStatus.DRAFT) }
    }
}
