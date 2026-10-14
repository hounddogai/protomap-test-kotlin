package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newQualificationAgreement("code-413", "label-628")
        assertEquals(QualificationAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newQualificationAgreement("code-413", "label-628")
        try {
            item.moveTo(QualificationAgreementStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationAgreementStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newQualificationAgreement("code-413", "label-628")
        item.status = QualificationAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(QualificationAgreementStatus.IDLE) }
    }
}
