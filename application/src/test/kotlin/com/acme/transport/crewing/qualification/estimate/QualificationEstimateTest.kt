package com.acme.transport.crewing.qualification.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationEstimate("code-185", "name-369")
        assertEquals(QualificationEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newQualificationEstimate("code-185", "name-369")
        try {
            item.moveTo(QualificationEstimateStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationEstimateStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationEstimate("code-185", "name-369")
        item.status = QualificationEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationEstimateStatus.OPEN) }
    }
}
