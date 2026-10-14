package com.acme.transport.terminal.dwell.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newDwellManifest("code-454", "title-44")
        assertEquals(DwellManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDwellManifest("code-454", "title-44")
        try {
            item.moveTo(DwellManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDwellManifest("code-454", "title-44")
        item.status = DwellManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(DwellManifestStatus.REQUESTED) }
    }
}
