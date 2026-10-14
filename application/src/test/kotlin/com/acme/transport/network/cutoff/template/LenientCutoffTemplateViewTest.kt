package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCutoffTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCutoffTemplate("code-492", "title-113")
        val view = toLenientCutoffTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCutoffTemplateViews(mutableListOf<LenientCutoffTemplate>(item, item)).size, "views")
    }
}
