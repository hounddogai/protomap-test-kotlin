package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledInspectionAgreementTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledInspectionAgreement("code-406", "name-538")
        assertEquals(ScheduledInspectionAgreementStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledInspectionAgreement("code-406", "name-538")
        try {
            item.moveTo(ScheduledInspectionAgreementStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledInspectionAgreementStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledInspectionAgreement("code-406", "name-538")
        item.status = ScheduledInspectionAgreementStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledInspectionAgreementStatus.NEW) }
    }
}
