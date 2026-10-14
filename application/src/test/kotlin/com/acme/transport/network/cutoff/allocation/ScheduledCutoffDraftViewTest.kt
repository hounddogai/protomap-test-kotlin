package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCutoffDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCutoffDraft("code-670", "label-29")
        val view = toScheduledCutoffDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCutoffDraftViews(mutableListOf<ScheduledCutoffDraft>(item, item)).size, "views")
    }
}
