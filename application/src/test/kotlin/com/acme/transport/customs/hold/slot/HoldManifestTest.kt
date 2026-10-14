package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newHoldManifest("code-482", "name-868")
        assertEquals(HoldManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newHoldManifest("code-482", "name-868")
        try {
            item.moveTo(HoldManifestStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldManifestStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newHoldManifest("code-482", "name-868")
        item.status = HoldManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(HoldManifestStatus.PLANNED) }
    }
}
