package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDefectAgreementTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualDefectAgreement("code-749", "title-743")
        assertEquals(ManualDefectAgreementStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualDefectAgreement("code-749", "title-743")
        try {
            item.moveTo(ManualDefectAgreementStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDefectAgreementStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualDefectAgreement("code-749", "title-743")
        item.status = ManualDefectAgreementStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualDefectAgreementStatus.PROPOSED) }
    }
}
