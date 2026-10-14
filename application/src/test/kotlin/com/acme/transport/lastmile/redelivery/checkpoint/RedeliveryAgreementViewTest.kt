package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryAgreement("code-219", "title-876")
        val view = toRedeliveryAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryAgreementViews(mutableListOf<RedeliveryAgreement>(item, item)).size, "views")
    }
}
