package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyQualificationMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyQualificationMetric("code-817", "label-793")
        assertEquals(LegacyQualificationMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyQualificationMetric("code-817", "label-793")
        try {
            item.moveTo(LegacyQualificationMetricStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyQualificationMetricStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyQualificationMetric("code-817", "label-793")
        item.status = LegacyQualificationMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyQualificationMetricStatus.PLANNED) }
    }
}
