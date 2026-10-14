package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedDeclarationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedDeclarationClaim("code-858", "label-957")
        val view = toWeightedDeclarationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedDeclarationClaimViews(mutableListOf<WeightedDeclarationClaim>(item, item)).size, "views")
    }
}
