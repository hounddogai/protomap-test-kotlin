package com.acme.transport.network.cutoff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCutoffManifestTest {
    @Test
    fun `starts in new`() {
        val item = newCappedCutoffManifest("code-532", "name-353")
        assertEquals(CappedCutoffManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedCutoffManifest("code-532", "name-353")
        try {
            item.moveTo(CappedCutoffManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCutoffManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedCutoffManifest("code-532", "name-353")
        item.status = CappedCutoffManifestStatus.FAILED
        assertFails("move") { item.moveTo(CappedCutoffManifestStatus.NEW) }
    }
}
