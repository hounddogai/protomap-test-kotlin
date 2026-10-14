package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedQualificationTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedQualificationTemplate("code-427", "title-910")
        assertEquals(BatchedQualificationTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedQualificationTemplate("code-427", "title-910")
        try {
            item.moveTo(BatchedQualificationTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedQualificationTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedQualificationTemplate("code-427", "title-910")
        item.status = BatchedQualificationTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedQualificationTemplateStatus.DRAFT) }
    }
}
