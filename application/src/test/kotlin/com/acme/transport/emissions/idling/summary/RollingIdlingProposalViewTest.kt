package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingIdlingProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingIdlingProposal("code-881", "name-250")
        val view = toRollingIdlingProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingIdlingProposalViews(mutableListOf<RollingIdlingProposal>(item, item)).size, "views")
    }
}
