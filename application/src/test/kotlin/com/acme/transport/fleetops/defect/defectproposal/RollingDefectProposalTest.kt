package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDefectProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingDefectProposal("code-681", "name-402")
        assertEquals(RollingDefectProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingDefectProposal("code-681", "name-402")
        try {
            item.moveTo(RollingDefectProposalStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDefectProposalStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingDefectProposal("code-681", "name-402")
        item.status = RollingDefectProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingDefectProposalStatus.PROPOSED) }
    }
}
