package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedQualificationCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedQualificationCheckpoint("code-605", "title-187")
        val view = toBatchedQualificationCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedQualificationCheckpointViews(mutableListOf<BatchedQualificationCheckpoint>(item, item)).size, "views")
    }
}
