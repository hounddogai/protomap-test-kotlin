package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNearMissAgreementTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientNearMissAgreement("code-379", "name-429")
        assertEquals(LenientNearMissAgreementStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientNearMissAgreement("code-379", "name-429")
        try {
            item.moveTo(LenientNearMissAgreementStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNearMissAgreementStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientNearMissAgreement("code-379", "name-429")
        item.status = LenientNearMissAgreementStatus.CLOSED
        assertFails("move") { item.moveTo(LenientNearMissAgreementStatus.DRAFT) }
    }
}
