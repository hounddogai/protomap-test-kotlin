package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newManualInspectionGroup("code-394", "title-419")
        assertEquals(ManualInspectionGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualInspectionGroup("code-394", "title-419")
        try {
            item.moveTo(ManualInspectionGroupStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionGroupStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualInspectionGroup("code-394", "title-419")
        item.status = ManualInspectionGroupStatus.CLOSED
        assertFails("move") { item.moveTo(ManualInspectionGroupStatus.DRAFT) }
    }
}
