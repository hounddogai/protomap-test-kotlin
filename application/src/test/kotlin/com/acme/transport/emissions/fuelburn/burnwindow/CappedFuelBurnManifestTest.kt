package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedFuelBurnManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedFuelBurnManifest("code-807", "title-54")
        assertEquals(CappedFuelBurnManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedFuelBurnManifest("code-807", "title-54")
        try {
            item.moveTo(CappedFuelBurnManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedFuelBurnManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedFuelBurnManifest("code-807", "title-54")
        item.status = CappedFuelBurnManifestStatus.CLOSED
        assertFails("move") { item.moveTo(CappedFuelBurnManifestStatus.DRAFT) }
    }
}
