package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedQualificationAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedQualificationAgreement("code-54", "name-192")
        assertEquals(CappedQualificationAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedQualificationAgreement("code-54", "name-192")
        try {
            item.moveTo(CappedQualificationAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedQualificationAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedQualificationAgreement("code-54", "name-192")
        item.status = CappedQualificationAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedQualificationAgreementStatus.REQUESTED) }
    }
}
