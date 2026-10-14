package com.acme.transport.fleetops.inspection.inspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictInspectionMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictInspectionMapping("code-90", "title-178")
        assertEquals(StrictInspectionMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictInspectionMapping("code-90", "title-178")
        try {
            item.moveTo(StrictInspectionMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictInspectionMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictInspectionMapping("code-90", "title-178")
        item.status = StrictInspectionMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictInspectionMappingStatus.PLANNED) }
    }
}
