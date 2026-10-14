package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingNodeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingNodeAgreement("code-608", "label-338")
        val view = toPendingNodeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingNodeAgreementViews(mutableListOf<PendingNodeAgreement>(item, item)).size, "views")
    }
}
