package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingIdlingProposalTest {
    @Test
    fun `starts in created`() {
        val item = newRollingIdlingProposal("code-881", "name-250")
        assertEquals(RollingIdlingProposalStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingIdlingProposal("code-881", "name-250")
        try {
            item.moveTo(RollingIdlingProposalStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingIdlingProposalStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingIdlingProposal("code-881", "name-250")
        item.status = RollingIdlingProposalStatus.RETIRED
        assertFails("move") { item.moveTo(RollingIdlingProposalStatus.CREATED) }
    }
}
