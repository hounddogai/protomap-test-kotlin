package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newQualificationMilestone("code-564", "name-235")
        assertEquals(QualificationMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newQualificationMilestone("code-564", "name-235")
        try {
            item.moveTo(QualificationMilestoneStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationMilestoneStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newQualificationMilestone("code-564", "name-235")
        item.status = QualificationMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(QualificationMilestoneStatus.OPEN) }
    }
}
