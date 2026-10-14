package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDeclarationBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDeclarationBatch("code-520", "name-708")
        val view = toManualDeclarationBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDeclarationBatchViews(mutableListOf<ManualDeclarationBatch>(item, item)).size, "views")
    }
}
