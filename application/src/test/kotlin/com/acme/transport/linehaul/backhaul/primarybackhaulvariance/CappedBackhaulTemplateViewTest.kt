package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedBackhaulTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedBackhaulTemplate("code-651", "title-39")
        val view = toCappedBackhaulTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedBackhaulTemplateViews(mutableListOf<CappedBackhaulTemplate>(item, item)).size, "views")
    }
}
