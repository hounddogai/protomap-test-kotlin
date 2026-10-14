package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalBackhaulManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalBackhaulManifestStore()
        val item = newSeasonalBackhaulManifest("code-774", "label-581")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
