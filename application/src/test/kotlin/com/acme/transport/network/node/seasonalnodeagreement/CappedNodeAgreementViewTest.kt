package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedNodeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedNodeAgreement("code-326", "title-60")
        val view = toCappedNodeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedNodeAgreementViews(mutableListOf<CappedNodeAgreement>(item, item)).size, "views")
    }
}
