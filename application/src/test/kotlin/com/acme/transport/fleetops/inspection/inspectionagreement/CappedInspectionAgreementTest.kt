package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedInspectionAgreement("code-768", "name-147")
        assertEquals(CappedInspectionAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCappedInspectionAgreement("code-768", "name-147")
        try {
            item.moveTo(CappedInspectionAgreementStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionAgreementStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedInspectionAgreement("code-768", "name-147")
        item.status = CappedInspectionAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedInspectionAgreementStatus.PROPOSED) }
    }
}
