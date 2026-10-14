package com.acme.transport.network.cutoff.weightedcutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCutoffManifestTest {
    @Test
    fun `starts in created`() {
        val item = newLenientCutoffManifest("code-358", "name-46")
        assertEquals(LenientCutoffManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientCutoffManifest("code-358", "name-46")
        try {
            item.moveTo(LenientCutoffManifestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCutoffManifestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientCutoffManifest("code-358", "name-46")
        item.status = LenientCutoffManifestStatus.RETIRED
        assertFails("move") { item.moveTo(LenientCutoffManifestStatus.CREATED) }
    }
}
