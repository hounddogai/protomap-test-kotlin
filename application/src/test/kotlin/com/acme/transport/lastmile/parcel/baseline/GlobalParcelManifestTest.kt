package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalParcelManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalParcelManifest("code-172", "title-178")
        assertEquals(GlobalParcelManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newGlobalParcelManifest("code-172", "title-178")
        try {
            item.moveTo(GlobalParcelManifestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalParcelManifestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalParcelManifest("code-172", "title-178")
        item.status = GlobalParcelManifestStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalParcelManifestStatus.DRAFT) }
    }
}
