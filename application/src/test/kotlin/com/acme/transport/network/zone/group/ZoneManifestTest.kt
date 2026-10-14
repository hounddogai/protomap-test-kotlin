package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newZoneManifest("code-445", "name-260")
        assertEquals(ZoneManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newZoneManifest("code-445", "name-260")
        try {
            item.moveTo(ZoneManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newZoneManifest("code-445", "name-260")
        item.status = ZoneManifestStatus.STOPPED
        assertFails("move") { item.moveTo(ZoneManifestStatus.IDLE) }
    }
}
