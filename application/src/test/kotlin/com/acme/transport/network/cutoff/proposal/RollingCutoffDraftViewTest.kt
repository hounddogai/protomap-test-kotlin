package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCutoffDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCutoffDraft("code-420", "name-964")
        val view = toRollingCutoffDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCutoffDraftViews(mutableListOf<RollingCutoffDraft>(item, item)).size, "views")
    }
}
