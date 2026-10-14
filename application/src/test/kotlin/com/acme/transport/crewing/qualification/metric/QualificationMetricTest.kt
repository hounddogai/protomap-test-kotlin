package com.acme.transport.crewing.qualification.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newQualificationMetric("code-656", "name-918")
        assertEquals(QualificationMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newQualificationMetric("code-656", "name-918")
        try {
            item.moveTo(QualificationMetricStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationMetricStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newQualificationMetric("code-656", "name-918")
        item.status = QualificationMetricStatus.CLOSED
        assertFails("move") { item.moveTo(QualificationMetricStatus.DRAFT) }
    }
}
