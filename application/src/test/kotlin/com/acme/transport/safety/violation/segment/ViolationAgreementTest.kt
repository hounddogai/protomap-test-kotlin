package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newViolationAgreement("code-709", "title-108")
        assertEquals(ViolationAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newViolationAgreement("code-709", "title-108")
        try {
            item.moveTo(ViolationAgreementStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationAgreementStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newViolationAgreement("code-709", "title-108")
        item.status = ViolationAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(ViolationAgreementStatus.OPEN) }
    }
}
