package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCrossDockAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCrossDockAgreement("code-767", "title-915")
        val view = toStrictCrossDockAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCrossDockAgreementViews(mutableListOf<StrictCrossDockAgreement>(item, item)).size, "views")
    }
}
