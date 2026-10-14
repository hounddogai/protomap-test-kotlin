package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionMappingTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalInspectionMapping("code-524", "name-275")
        assertEquals(GlobalInspectionMappingStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalInspectionMapping("code-524", "name-275")
        try {
            item.moveTo(GlobalInspectionMappingStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionMappingStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalInspectionMapping("code-524", "name-275")
        item.status = GlobalInspectionMappingStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalInspectionMappingStatus.OPEN) }
    }
}
