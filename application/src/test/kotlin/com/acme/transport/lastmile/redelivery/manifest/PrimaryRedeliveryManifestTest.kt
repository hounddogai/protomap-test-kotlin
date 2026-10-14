package com.acme.transport.lastmile.redelivery.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryManifestTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryRedeliveryManifest("code-721", "title-689")
        assertEquals(PrimaryRedeliveryManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryRedeliveryManifest("code-721", "title-689")
        try {
            item.moveTo(PrimaryRedeliveryManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryRedeliveryManifest("code-721", "title-689")
        item.status = PrimaryRedeliveryManifestStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryRedeliveryManifestStatus.CREATED) }
    }
}
