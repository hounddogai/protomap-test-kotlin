package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityManifestTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalCapacityManifest("code-9", "title-394")
        assertEquals(RegionalCapacityManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalCapacityManifest("code-9", "title-394")
        try {
            item.moveTo(RegionalCapacityManifestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityManifestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalCapacityManifest("code-9", "title-394")
        item.status = RegionalCapacityManifestStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalCapacityManifestStatus.CREATED) }
    }
}
