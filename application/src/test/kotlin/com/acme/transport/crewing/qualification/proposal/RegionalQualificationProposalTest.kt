package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationProposalTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalQualificationProposal("code-171", "title-258")
        assertEquals(RegionalQualificationProposalStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalQualificationProposal("code-171", "title-258")
        try {
            item.moveTo(RegionalQualificationProposalStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationProposalStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalQualificationProposal("code-171", "title-258")
        item.status = RegionalQualificationProposalStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalQualificationProposalStatus.REQUESTED) }
    }
}
