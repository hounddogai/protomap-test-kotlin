package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDeclarationTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDeclarationTarget("code-281", "label-551")
        val view = toStrictDeclarationTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDeclarationTargetViews(mutableListOf<StrictDeclarationTarget>(item, item)).size, "views")
    }
}
