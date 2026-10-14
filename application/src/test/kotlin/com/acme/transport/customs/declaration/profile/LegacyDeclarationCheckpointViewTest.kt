package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDeclarationCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDeclarationCheckpoint("code-540", "name-493")
        val view = toLegacyDeclarationCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDeclarationCheckpointViews(mutableListOf<LegacyDeclarationCheckpoint>(item, item)).size, "views")
    }
}
