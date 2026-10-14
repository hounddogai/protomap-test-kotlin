package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDeclarationBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newManualDeclarationBatch("code-520", "name-708")
        assertEquals(ManualDeclarationBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newManualDeclarationBatch("code-520", "name-708")
        try {
            item.moveTo(ManualDeclarationBatchStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDeclarationBatchStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualDeclarationBatch("code-520", "name-708")
        item.status = ManualDeclarationBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualDeclarationBatchStatus.REQUESTED) }
    }
}
