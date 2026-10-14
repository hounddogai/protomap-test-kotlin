package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedSwapAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedSwapAgreement("code-443", "name-340")
        val view = toCappedSwapAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedSwapAgreementViews(mutableListOf<CappedSwapAgreement>(item, item)).size, "views")
    }
}
