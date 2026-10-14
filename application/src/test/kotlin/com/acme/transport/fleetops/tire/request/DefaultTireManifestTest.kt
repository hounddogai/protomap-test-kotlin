package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTireManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultTireManifest("code-551", "title-445")
        assertEquals(DefaultTireManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultTireManifest("code-551", "title-445")
        try {
            item.moveTo(DefaultTireManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTireManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultTireManifest("code-551", "title-445")
        item.status = DefaultTireManifestStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultTireManifestStatus.IDLE) }
    }
}
