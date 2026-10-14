package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorProposalTest {
    @Test
    fun `starts in open`() {
        val item = newEmissionFactorProposal("code-386", "title-6")
        assertEquals(EmissionFactorProposalStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newEmissionFactorProposal("code-386", "title-6")
        try {
            item.moveTo(EmissionFactorProposalStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorProposalStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEmissionFactorProposal("code-386", "title-6")
        item.status = EmissionFactorProposalStatus.ARCHIVED
        assertFails("move") { item.moveTo(EmissionFactorProposalStatus.OPEN) }
    }
}
