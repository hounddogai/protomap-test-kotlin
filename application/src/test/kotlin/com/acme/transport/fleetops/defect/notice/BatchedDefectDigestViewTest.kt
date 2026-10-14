package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDefectDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDefectDigest("code-218", "label-267")
        val view = toBatchedDefectDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDefectDigestViews(mutableListOf<BatchedDefectDigest>(item, item)).size, "views")
    }
}
