package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffPolicy("code-86", "title-456")
        val view = toCutoffPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffPolicyViews(mutableListOf<CutoffPolicy>(item, item)).size, "views")
    }
}
