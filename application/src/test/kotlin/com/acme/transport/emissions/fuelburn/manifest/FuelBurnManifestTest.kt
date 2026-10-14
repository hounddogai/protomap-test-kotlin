package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnManifestTest {
    @Test
    fun `starts in open`() {
        val item = newFuelBurnManifest("code-835", "label-772")
        assertEquals(FuelBurnManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFuelBurnManifest("code-835", "label-772")
        try {
            item.moveTo(FuelBurnManifestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnManifestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelBurnManifest("code-835", "label-772")
        item.status = FuelBurnManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelBurnManifestStatus.OPEN) }
    }
}
