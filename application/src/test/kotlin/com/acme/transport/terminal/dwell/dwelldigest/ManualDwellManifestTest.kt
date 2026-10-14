package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDwellManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualDwellManifest("code-758", "name-571")
        assertEquals(ManualDwellManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newManualDwellManifest("code-758", "name-571")
        try {
            item.moveTo(ManualDwellManifestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDwellManifestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualDwellManifest("code-758", "name-571")
        item.status = ManualDwellManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualDwellManifestStatus.PROPOSED) }
    }
}
