package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        assertEquals(GlobalCoachingAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        try {
            item.moveTo(GlobalCoachingAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        item.status = GlobalCoachingAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalCoachingAgreementStatus.DRAFT) }
    }
}
