package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyEmissionFactorManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyEmissionFactorManifestStore()
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
