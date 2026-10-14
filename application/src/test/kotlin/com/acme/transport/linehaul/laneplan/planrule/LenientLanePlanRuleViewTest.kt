package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientLanePlanRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientLanePlanRule("code-396", "title-352")
        val view = toLenientLanePlanRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientLanePlanRuleViews(mutableListOf<LenientLanePlanRule>(item, item)).size, "views")
    }
}
