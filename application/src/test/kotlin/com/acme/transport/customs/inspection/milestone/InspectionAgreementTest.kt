package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionAgreement("code-60", "title-59")
        assertEquals(InspectionAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newInspectionAgreement("code-60", "title-59")
        try {
            item.moveTo(InspectionAgreementStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAgreementStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionAgreement("code-60", "title-59")
        item.status = InspectionAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionAgreementStatus.CREATED) }
    }
}
