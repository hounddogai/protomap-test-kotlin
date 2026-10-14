package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCutoffAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCutoffAgreement("code-555", "title-911")
        val view = toDefaultCutoffAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCutoffAgreementViews(mutableListOf<DefaultCutoffAgreement>(item, item)).size, "views")
    }
}
