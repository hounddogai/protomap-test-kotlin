package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedQualificationBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedQualificationBundle("code-704", "label-462")
        val view = toBatchedQualificationBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedQualificationBundleViews(mutableListOf<BatchedQualificationBundle>(item, item)).size, "views")
    }
}
