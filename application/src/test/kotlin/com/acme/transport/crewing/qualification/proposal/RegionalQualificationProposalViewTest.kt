package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalQualificationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalQualificationProposal("code-171", "title-258")
        val view = toRegionalQualificationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalQualificationProposalViews(mutableListOf<RegionalQualificationProposal>(item, item)).size, "views")
    }
}
