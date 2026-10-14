package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServiceAreaManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalServiceAreaManifest("code-958", "name-815")
        assertEquals(RegionalServiceAreaManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalServiceAreaManifest("code-958", "name-815")
        try {
            item.moveTo(RegionalServiceAreaManifestStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServiceAreaManifestStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalServiceAreaManifest("code-958", "name-815")
        item.status = RegionalServiceAreaManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalServiceAreaManifestStatus.PLANNED) }
    }
}
