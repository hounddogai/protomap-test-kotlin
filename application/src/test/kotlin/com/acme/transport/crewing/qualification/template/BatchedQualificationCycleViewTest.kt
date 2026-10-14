package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedQualificationCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedQualificationCycle("code-337", "title-903")
        val view = toBatchedQualificationCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedQualificationCycleViews(mutableListOf<BatchedQualificationCycle>(item, item)).size, "views")
    }
}
