package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyEmissionFactorManifestTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        assertEquals(LegacyEmissionFactorManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        try {
            item.moveTo(LegacyEmissionFactorManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyEmissionFactorManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        item.status = LegacyEmissionFactorManifestStatus.FAILED
        assertFails("move") { item.moveTo(LegacyEmissionFactorManifestStatus.NEW) }
    }
}
