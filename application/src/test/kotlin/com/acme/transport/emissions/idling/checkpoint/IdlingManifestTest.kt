package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingManifestTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingManifest("code-66", "title-10")
        assertEquals(IdlingManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingManifest("code-66", "title-10")
        try {
            item.moveTo(IdlingManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingManifest("code-66", "title-10")
        item.status = IdlingManifestStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingManifestStatus.CREATED) }
    }
}
