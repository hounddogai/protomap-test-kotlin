package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalDeclarationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalDeclarationTarget("code-719", "name-11")
        val view = toRegionalDeclarationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalDeclarationTargetViews(mutableListOf<RegionalDeclarationTarget>(item, item)).size, "views")
    }
}
