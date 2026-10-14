package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDefectManifestTest {
    @Test
    fun `starts in created`() {
        val item = newTieredDefectManifest("code-287", "title-298")
        assertEquals(TieredDefectManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTieredDefectManifest("code-287", "title-298")
        try {
            item.moveTo(TieredDefectManifestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDefectManifestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredDefectManifest("code-287", "title-298")
        item.status = TieredDefectManifestStatus.RETIRED
        assertFails("move") { item.moveTo(TieredDefectManifestStatus.CREATED) }
    }
}
