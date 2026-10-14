package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorNotice("code-819", "label-472")
        val view = toEmissionFactorNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorNoticeViews(mutableListOf<EmissionFactorNotice>(item, item)).size, "views")
    }
}
