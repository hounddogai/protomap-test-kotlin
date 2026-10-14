package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDisputeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        val view = toDefaultDisputeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDisputeTemplateViews(mutableListOf<DefaultDisputeTemplate>(item, item)).size, "views")
    }
}
