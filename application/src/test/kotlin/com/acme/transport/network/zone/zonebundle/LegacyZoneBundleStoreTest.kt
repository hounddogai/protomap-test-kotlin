package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyZoneBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyZoneBundleStore()
        val item = newLegacyZoneBundle("code-506", "name-860")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
