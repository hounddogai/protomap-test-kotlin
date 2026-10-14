package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZoneManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredZoneManifest("code-931", "label-109")
        assertEquals(TieredZoneManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTieredZoneManifest("code-931", "label-109")
        try {
            item.moveTo(TieredZoneManifestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZoneManifestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredZoneManifest("code-931", "label-109")
        item.status = TieredZoneManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredZoneManifestStatus.PROPOSED) }
    }
}
