package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardAgreement("code-385", "title-445")
        val view = toTrailerYardAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardAgreementViews(mutableListOf<TrailerYardAgreement>(item, item)).size, "views")
    }
}
