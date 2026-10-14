package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDeclarationAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDeclarationAdjustment("code-352", "label-518")
        val view = toManualDeclarationAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDeclarationAdjustmentViews(mutableListOf<ManualDeclarationAdjustment>(item, item)).size, "views")
    }
}
