package com.acme.transport.lastmile.drop.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newDropManifest("code-919", "label-480")
        assertEquals(DropManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDropManifest("code-919", "label-480")
        try {
            item.moveTo(DropManifestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropManifestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDropManifest("code-919", "label-480")
        item.status = DropManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DropManifestStatus.PROPOSED) }
    }
}
