package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDeclarationSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualDeclarationSnapshot("code-171", "name-498")
        assertEquals(ManualDeclarationSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualDeclarationSnapshot("code-171", "name-498")
        try {
            item.moveTo(ManualDeclarationSnapshotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDeclarationSnapshotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualDeclarationSnapshot("code-171", "name-498")
        item.status = ManualDeclarationSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualDeclarationSnapshotStatus.PROPOSED) }
    }
}
