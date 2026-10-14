package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffTemplate("code-638", "label-457")
        val view = toCutoffTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffTemplateViews(mutableListOf<CutoffTemplate>(item, item)).size, "views")
    }
}
