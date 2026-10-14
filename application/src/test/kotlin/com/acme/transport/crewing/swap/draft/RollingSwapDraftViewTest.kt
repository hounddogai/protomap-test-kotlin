package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingSwapDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingSwapDraft("code-406", "name-933")
        val view = toRollingSwapDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingSwapDraftViews(mutableListOf<RollingSwapDraft>(item, item)).size, "views")
    }
}
