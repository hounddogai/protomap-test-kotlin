package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedLanePlanPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedLanePlanPolicy("code-707", "name-921")
        val view = toCappedLanePlanPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedLanePlanPolicyViews(mutableListOf<CappedLanePlanPolicy>(item, item)).size, "views")
    }
}
