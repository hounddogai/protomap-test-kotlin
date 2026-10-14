package com.acme.transport.customs.declaration.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDeclarationSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDeclarationSnapshot("code-221", "label-553")
        val view = toBatchedDeclarationSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDeclarationSnapshotViews(mutableListOf<BatchedDeclarationSnapshot>(item, item)).size, "views")
    }
}
