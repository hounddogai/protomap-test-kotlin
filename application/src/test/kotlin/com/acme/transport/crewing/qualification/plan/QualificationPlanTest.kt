package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationPlanTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationPlan("code-539", "label-947")
        assertEquals(QualificationPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newQualificationPlan("code-539", "label-947")
        try {
            item.moveTo(QualificationPlanStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationPlanStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationPlan("code-539", "label-947")
        item.status = QualificationPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationPlanStatus.OPEN) }
    }
}
