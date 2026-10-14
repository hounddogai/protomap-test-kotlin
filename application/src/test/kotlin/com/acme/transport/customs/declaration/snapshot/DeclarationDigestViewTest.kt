package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationDigest("code-349", "name-930")
        val view = toDeclarationDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationDigestViews(mutableListOf<DeclarationDigest>(item, item)).size, "views")
    }
}
