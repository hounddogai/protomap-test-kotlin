package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultQualificationAgreementTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultQualificationAgreement("code-922", "title-433")
        assertEquals(DefaultQualificationAgreementStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDefaultQualificationAgreement("code-922", "title-433")
        try {
            item.moveTo(DefaultQualificationAgreementStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultQualificationAgreementStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultQualificationAgreement("code-922", "title-433")
        item.status = DefaultQualificationAgreementStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultQualificationAgreementStatus.OPEN) }
    }
}
