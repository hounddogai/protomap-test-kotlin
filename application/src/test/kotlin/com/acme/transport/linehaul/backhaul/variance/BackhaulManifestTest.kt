package com.acme.transport.linehaul.backhaul.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newBackhaulManifest("code-635", "label-947")
        assertEquals(BackhaulManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBackhaulManifest("code-635", "label-947")
        try {
            item.moveTo(BackhaulManifestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulManifestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBackhaulManifest("code-635", "label-947")
        item.status = BackhaulManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BackhaulManifestStatus.PROPOSED) }
    }
}
