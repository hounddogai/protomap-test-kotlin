package com.acme.transport.customs.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionMapping("code-182", "label-893")
        assertEquals(InspectionMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newInspectionMapping("code-182", "label-893")
        try {
            item.moveTo(InspectionMappingStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionMappingStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionMapping("code-182", "label-893")
        item.status = InspectionMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionMappingStatus.PLANNED) }
    }
}
