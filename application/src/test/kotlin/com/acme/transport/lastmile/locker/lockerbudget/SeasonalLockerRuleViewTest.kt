package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalLockerRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalLockerRule("code-617", "title-813")
        val view = toSeasonalLockerRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalLockerRuleViews(mutableListOf<SeasonalLockerRule>(item, item)).size, "views")
    }
}
