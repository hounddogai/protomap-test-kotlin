package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakAgreement("code-402", "title-85")
        val view = toRestBreakAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakAgreementViews(mutableListOf<RestBreakAgreement>(item, item)).size, "views")
    }
}
