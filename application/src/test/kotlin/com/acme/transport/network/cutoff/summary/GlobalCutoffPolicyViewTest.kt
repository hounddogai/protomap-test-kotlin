package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCutoffPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCutoffPolicy("code-760", "label-609")
        val view = toGlobalCutoffPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCutoffPolicyViews(mutableListOf<GlobalCutoffPolicy>(item, item)).size, "views")
    }
}
