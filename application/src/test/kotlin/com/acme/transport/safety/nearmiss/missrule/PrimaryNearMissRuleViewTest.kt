package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryNearMissRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryNearMissRule("code-931", "title-602")
        val view = toPrimaryNearMissRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryNearMissRuleViews(mutableListOf<PrimaryNearMissRule>(item, item)).size, "views")
    }
}
