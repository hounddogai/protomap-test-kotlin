package com.acme.transport.customs.declaration.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingDeclarationManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingDeclarationManifest("code-538", "name-7")
        val view = toRollingDeclarationManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingDeclarationManifestViews(mutableListOf<RollingDeclarationManifest>(item, item)).size, "views")
    }
}
