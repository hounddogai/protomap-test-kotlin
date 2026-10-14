package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredQualificationMetricTest {
    @Test
    fun `starts in open`() {
        val item = newTieredQualificationMetric("code-381", "title-351")
        assertEquals(TieredQualificationMetricStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredQualificationMetric("code-381", "title-351")
        try {
            item.moveTo(TieredQualificationMetricStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredQualificationMetricStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredQualificationMetric("code-381", "title-351")
        item.status = TieredQualificationMetricStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredQualificationMetricStatus.OPEN) }
    }
}
