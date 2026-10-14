package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredInspectionGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredInspectionGroup("code-985", "title-822")
        assertEquals(TieredInspectionGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredInspectionGroup("code-985", "title-822")
        try {
            item.moveTo(TieredInspectionGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredInspectionGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredInspectionGroup("code-985", "title-822")
        item.status = TieredInspectionGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredInspectionGroupStatus.PROPOSED) }
    }
}
