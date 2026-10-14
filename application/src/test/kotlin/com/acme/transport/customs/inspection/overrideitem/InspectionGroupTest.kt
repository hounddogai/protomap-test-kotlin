package com.acme.transport.customs.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionGroup("code-596", "label-484")
        assertEquals(InspectionGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newInspectionGroup("code-596", "label-484")
        try {
            item.moveTo(InspectionGroupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionGroupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionGroup("code-596", "label-484")
        item.status = InspectionGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionGroupStatus.REQUESTED) }
    }
}
