package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveQualificationProposalTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveQualificationProposal("code-237", "title-699")
        assertEquals(AdaptiveQualificationProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveQualificationProposal("code-237", "title-699")
        try {
            item.moveTo(AdaptiveQualificationProposalStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveQualificationProposalStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveQualificationProposal("code-237", "title-699")
        item.status = AdaptiveQualificationProposalStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveQualificationProposalStatus.CREATED) }
    }
}
