package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientDeclarationCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientDeclarationCheckpoint("code-674", "label-973")
        val view = toLenientDeclarationCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientDeclarationCheckpointViews(mutableListOf<LenientDeclarationCheckpoint>(item, item)).size, "views")
    }
}
