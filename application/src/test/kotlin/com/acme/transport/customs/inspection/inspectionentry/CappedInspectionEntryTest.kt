package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedInspectionEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedInspectionEntry("code-106", "title-259")
        assertEquals(CappedInspectionEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedInspectionEntry("code-106", "title-259")
        try {
            item.moveTo(CappedInspectionEntryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedInspectionEntryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedInspectionEntry("code-106", "title-259")
        item.status = CappedInspectionEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedInspectionEntryStatus.PROPOSED) }
    }
}
