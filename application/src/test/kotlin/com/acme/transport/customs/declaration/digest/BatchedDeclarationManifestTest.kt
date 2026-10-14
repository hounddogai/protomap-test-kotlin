package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDeclarationManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedDeclarationManifest("code-66", "name-819")
        assertEquals(BatchedDeclarationManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedDeclarationManifest("code-66", "name-819")
        try {
            item.moveTo(BatchedDeclarationManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDeclarationManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedDeclarationManifest("code-66", "name-819")
        item.status = BatchedDeclarationManifestStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedDeclarationManifestStatus.IDLE) }
    }
}
