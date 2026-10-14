package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerManifestTest {
    @Test
    fun `starts in new`() {
        val item = newStrictTrailerManifest("code-524", "title-351")
        assertEquals(StrictTrailerManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictTrailerManifest("code-524", "title-351")
        try {
            item.moveTo(StrictTrailerManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictTrailerManifest("code-524", "title-351")
        item.status = StrictTrailerManifestStatus.FAILED
        assertFails("move") { item.moveTo(StrictTrailerManifestStatus.NEW) }
    }
}
