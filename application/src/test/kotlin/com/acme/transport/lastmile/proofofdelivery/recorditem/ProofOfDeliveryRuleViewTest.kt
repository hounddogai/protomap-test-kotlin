package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfDeliveryRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newProofOfDeliveryRule("code-957", "label-960")
        val view = toProofOfDeliveryRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toProofOfDeliveryRuleViews(mutableListOf<ProofOfDeliveryRule>(item, item)).size, "views")
    }
}
