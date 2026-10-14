package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCoachingAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictCoachingAgreement("code-928", "label-270")
        assertEquals(StrictCoachingAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictCoachingAgreement("code-928", "label-270")
        try {
            item.moveTo(StrictCoachingAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCoachingAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictCoachingAgreement("code-928", "label-270")
        item.status = StrictCoachingAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(StrictCoachingAgreementStatus.DRAFT) }
    }
}
