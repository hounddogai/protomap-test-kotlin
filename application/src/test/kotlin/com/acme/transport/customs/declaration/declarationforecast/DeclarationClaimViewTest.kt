package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationClaim("code-283", "name-141")
        val view = toDeclarationClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationClaimViews(mutableListOf<DeclarationClaim>(item, item)).size, "views")
    }
}
