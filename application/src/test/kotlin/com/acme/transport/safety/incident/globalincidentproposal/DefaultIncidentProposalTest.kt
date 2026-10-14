package com.acme.transport.safety.incident.globalincidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIncidentProposalTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultIncidentProposal("code-938", "label-536")
        assertEquals(DefaultIncidentProposalStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultIncidentProposal("code-938", "label-536")
        try {
            item.moveTo(DefaultIncidentProposalStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIncidentProposalStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultIncidentProposal("code-938", "label-536")
        item.status = DefaultIncidentProposalStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultIncidentProposalStatus.PROPOSED) }
    }
}
