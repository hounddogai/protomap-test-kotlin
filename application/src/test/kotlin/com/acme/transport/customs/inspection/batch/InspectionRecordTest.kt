package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRecordTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionRecord("code-934", "title-908")
        assertEquals(InspectionRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newInspectionRecord("code-934", "title-908")
        try {
            item.moveTo(InspectionRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionRecord("code-934", "title-908")
        item.status = InspectionRecordStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionRecordStatus.CREATED) }
    }
}
