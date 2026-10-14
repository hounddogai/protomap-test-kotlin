package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingNodeDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingNodeDraft("code-445", "title-608")
        val view = toRollingNodeDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingNodeDraftViews(mutableListOf<RollingNodeDraft>(item, item)).size, "views")
    }
}
