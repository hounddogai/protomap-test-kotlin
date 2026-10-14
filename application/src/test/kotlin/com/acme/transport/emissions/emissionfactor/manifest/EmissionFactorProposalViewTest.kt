package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorProposal("code-386", "title-6")
        val view = toEmissionFactorProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorProposalViews(mutableListOf<EmissionFactorProposal>(item, item)).size, "views")
    }
}
