package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNearMissExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNearMissException("code-210", "name-917")
        val view = toAdaptiveNearMissExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNearMissExceptionViews(mutableListOf<AdaptiveNearMissException>(item, item)).size, "views")
    }
}
