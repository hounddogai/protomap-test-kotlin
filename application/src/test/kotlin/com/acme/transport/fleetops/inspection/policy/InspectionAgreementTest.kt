package com.acme.transport.fleetops.inspection.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionAgreement("code-527", "label-322")
        assertEquals(InspectionAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newInspectionAgreement("code-527", "label-322")
        try {
            item.moveTo(InspectionAgreementStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAgreementStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionAgreement("code-527", "label-322")
        item.status = InspectionAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionAgreementStatus.PLANNED) }
    }
}
