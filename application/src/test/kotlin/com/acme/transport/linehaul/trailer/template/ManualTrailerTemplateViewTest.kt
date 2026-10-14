package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTrailerTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTrailerTemplate("code-673", "title-331")
        val view = toManualTrailerTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTrailerTemplateViews(mutableListOf<ManualTrailerTemplate>(item, item)).size, "views")
    }
}
