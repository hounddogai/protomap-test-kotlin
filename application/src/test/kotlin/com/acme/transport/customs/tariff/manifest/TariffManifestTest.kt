package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TariffManifestTest {
    @Test
    fun `starts in new`() {
        val item = newTariffManifest("code-317", "name-817")
        assertEquals(TariffManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTariffManifest("code-317", "name-817")
        try {
            item.moveTo(TariffManifestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TariffManifestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTariffManifest("code-317", "name-817")
        item.status = TariffManifestStatus.FAILED
        assertFails("move") { item.moveTo(TariffManifestStatus.NEW) }
    }
}
