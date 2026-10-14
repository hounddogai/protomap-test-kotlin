package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceTarget("code-202", "title-797")
        val view = toClearanceTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceTargetViews(mutableListOf<ClearanceTarget>(item, item)).size, "views")
    }
}
