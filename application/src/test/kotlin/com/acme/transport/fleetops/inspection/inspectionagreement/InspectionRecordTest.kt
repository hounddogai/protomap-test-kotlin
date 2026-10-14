package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionRecord("code-715", "name-780")
        assertEquals(InspectionRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newInspectionRecord("code-715", "name-780")
        try {
            item.moveTo(InspectionRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionRecord("code-715", "name-780")
        item.status = InspectionRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionRecordStatus.PROPOSED) }
    }
}
