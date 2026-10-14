package com.acme.transport.crewing.qualification.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingQualificationMetricTest {
    @Test
    fun `starts in open`() {
        val item = newPendingQualificationMetric("code-268", "label-760")
        assertEquals(PendingQualificationMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPendingQualificationMetric("code-268", "label-760")
        try {
            item.moveTo(PendingQualificationMetricStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingQualificationMetricStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingQualificationMetric("code-268", "label-760")
        item.status = PendingQualificationMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingQualificationMetricStatus.OPEN) }
    }
}
