package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalEvChargeManifestTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalEvChargeManifest("code-537", "label-549")
        assertEquals(RegionalEvChargeManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRegionalEvChargeManifest("code-537", "label-549")
        try {
            item.moveTo(RegionalEvChargeManifestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalEvChargeManifestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalEvChargeManifest("code-537", "label-549")
        item.status = RegionalEvChargeManifestStatus.FAILED
        assertFails("move") { item.moveTo(RegionalEvChargeManifestStatus.NEW) }
    }
}
