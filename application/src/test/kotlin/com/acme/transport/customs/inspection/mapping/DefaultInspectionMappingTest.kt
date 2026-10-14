package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultInspectionMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultInspectionMapping("code-762", "label-412")
        assertEquals(DefaultInspectionMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultInspectionMapping("code-762", "label-412")
        try {
            item.moveTo(DefaultInspectionMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultInspectionMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultInspectionMapping("code-762", "label-412")
        item.status = DefaultInspectionMappingStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultInspectionMappingStatus.IDLE) }
    }
}
