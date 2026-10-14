package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDeclarationVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDeclarationVariance("code-382", "label-118")
        val view = toPendingDeclarationVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDeclarationVarianceViews(mutableListOf<PendingDeclarationVariance>(item, item)).size, "views")
    }
}
