package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class QualificationProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newQualificationProposal("code-607", "name-671")
        val view = toQualificationProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toQualificationProposalViews(mutableListOf<QualificationProposal>(item, item)).size, "views")
    }
}
