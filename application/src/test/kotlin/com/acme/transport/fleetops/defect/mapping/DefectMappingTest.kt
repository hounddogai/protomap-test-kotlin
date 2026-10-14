package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectMappingTest {
    @Test
    fun `starts in new`() {
        val item = newDefectMapping("code-774", "label-296")
        assertEquals(DefectMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefectMapping("code-774", "label-296")
        try {
            item.moveTo(DefectMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefectMapping("code-774", "label-296")
        item.status = DefectMappingStatus.FAILED
        assertFails("move") { item.moveTo(DefectMappingStatus.NEW) }
    }
}
