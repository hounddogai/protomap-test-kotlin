package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationEstimate("code-855", "title-335")
        val view = toDeclarationEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationEstimateViews(mutableListOf<DeclarationEstimate>(item, item)).size, "views")
    }
}
