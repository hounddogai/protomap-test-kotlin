package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDeclarationCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDeclarationCheckpoint("code-817", "name-806")
        val view = toBatchedDeclarationCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDeclarationCheckpointViews(mutableListOf<BatchedDeclarationCheckpoint>(item, item)).size, "views")
    }
}
