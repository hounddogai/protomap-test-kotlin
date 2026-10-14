package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectRecordTest {
    @Test
    fun `starts in created`() {
        val item = newDefectRecord("code-731", "label-534")
        assertEquals(DefectRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefectRecord("code-731", "label-534")
        try {
            item.moveTo(DefectRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefectRecord("code-731", "label-534")
        item.status = DefectRecordStatus.RETIRED
        assertFails("move") { item.moveTo(DefectRecordStatus.CREATED) }
    }
}
