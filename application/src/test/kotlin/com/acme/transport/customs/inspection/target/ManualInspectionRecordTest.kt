package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualInspectionRecord("code-835", "name-450")
        assertEquals(ManualInspectionRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualInspectionRecord("code-835", "name-450")
        try {
            item.moveTo(ManualInspectionRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualInspectionRecord("code-835", "name-450")
        item.status = ManualInspectionRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualInspectionRecordStatus.PROPOSED) }
    }
}
