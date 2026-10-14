package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryInspectionAgreement("code-115", "label-285")
        assertEquals(PrimaryInspectionAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPrimaryInspectionAgreement("code-115", "label-285")
        try {
            item.moveTo(PrimaryInspectionAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryInspectionAgreement("code-115", "label-285")
        item.status = PrimaryInspectionAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryInspectionAgreementStatus.REQUESTED) }
    }
}
