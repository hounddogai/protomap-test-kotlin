package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newQualificationProposal("code-607", "name-671")
        assertEquals(QualificationProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newQualificationProposal("code-607", "name-671")
        try {
            item.moveTo(QualificationProposalStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationProposalStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newQualificationProposal("code-607", "name-671")
        item.status = QualificationProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(QualificationProposalStatus.PLANNED) }
    }
}
