package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientProofOfDeliveryRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        val view = toLenientProofOfDeliveryRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientProofOfDeliveryRuleViews(mutableListOf<LenientProofOfDeliveryRule>(item, item)).size, "views")
    }
}
