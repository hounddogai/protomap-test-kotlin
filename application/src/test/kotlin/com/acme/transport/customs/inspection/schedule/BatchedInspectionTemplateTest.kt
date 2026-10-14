package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedInspectionTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedInspectionTemplate("code-443", "name-651")
        assertEquals(BatchedInspectionTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedInspectionTemplate("code-443", "name-651")
        try {
            item.moveTo(BatchedInspectionTemplateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedInspectionTemplateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedInspectionTemplate("code-443", "name-651")
        item.status = BatchedInspectionTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedInspectionTemplateStatus.CREATED) }
    }
}
