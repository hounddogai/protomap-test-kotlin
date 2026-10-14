package com.acme.transport.customs.declaration.declarationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDeclarationSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalDeclarationSnapshot("code-832", "label-706")
        assertEquals(GlobalDeclarationSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newGlobalDeclarationSnapshot("code-832", "label-706")
        try {
            item.moveTo(GlobalDeclarationSnapshotStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDeclarationSnapshotStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalDeclarationSnapshot("code-832", "label-706")
        item.status = GlobalDeclarationSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(GlobalDeclarationSnapshotStatus.NEW) }
    }
}
