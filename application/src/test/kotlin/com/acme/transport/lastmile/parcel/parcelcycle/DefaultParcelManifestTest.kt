package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultParcelManifestTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultParcelManifest("code-917", "label-625")
        assertEquals(DefaultParcelManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDefaultParcelManifest("code-917", "label-625")
        try {
            item.moveTo(DefaultParcelManifestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultParcelManifestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultParcelManifest("code-917", "label-625")
        item.status = DefaultParcelManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultParcelManifestStatus.OPEN) }
    }
}
