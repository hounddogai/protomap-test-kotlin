package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDeclarationManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDeclarationManifest("code-325", "label-200")
        val view = toLegacyDeclarationManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDeclarationManifestViews(mutableListOf<LegacyDeclarationManifest>(item, item)).size, "views")
    }
}
