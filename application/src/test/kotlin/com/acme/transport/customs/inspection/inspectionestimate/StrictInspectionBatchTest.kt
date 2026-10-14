package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictInspectionBatchTest {
    @Test
    fun `starts in new`() {
        val item = newStrictInspectionBatch("code-692", "label-693")
        assertEquals(StrictInspectionBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newStrictInspectionBatch("code-692", "label-693")
        try {
            item.moveTo(StrictInspectionBatchStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictInspectionBatchStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictInspectionBatch("code-692", "label-693")
        item.status = StrictInspectionBatchStatus.FAILED
        assertFails("move") { item.moveTo(StrictInspectionBatchStatus.NEW) }
    }
}
