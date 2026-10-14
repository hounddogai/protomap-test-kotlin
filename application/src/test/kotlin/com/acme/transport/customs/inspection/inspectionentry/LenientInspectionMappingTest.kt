package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionMappingTest {
    @Test
    fun `starts in created`() {
        val item = newLenientInspectionMapping("code-174", "title-709")
        assertEquals(LenientInspectionMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientInspectionMapping("code-174", "title-709")
        try {
            item.moveTo(LenientInspectionMappingStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionMappingStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientInspectionMapping("code-174", "title-709")
        item.status = LenientInspectionMappingStatus.RETIRED
        assertFails("move") { item.moveTo(LenientInspectionMappingStatus.CREATED) }
    }
}
