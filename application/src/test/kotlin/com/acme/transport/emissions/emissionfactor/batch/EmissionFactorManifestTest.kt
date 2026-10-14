package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EmissionFactorManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newEmissionFactorManifest("code-91", "title-466")
        assertEquals(EmissionFactorManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newEmissionFactorManifest("code-91", "title-466")
        try {
            item.moveTo(EmissionFactorManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EmissionFactorManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEmissionFactorManifest("code-91", "title-466")
        item.status = EmissionFactorManifestStatus.STOPPED
        assertFails("move") { item.moveTo(EmissionFactorManifestStatus.IDLE) }
    }
}
