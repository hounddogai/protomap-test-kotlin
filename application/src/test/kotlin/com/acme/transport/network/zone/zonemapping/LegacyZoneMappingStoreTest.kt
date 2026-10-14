package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyZoneMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyZoneMappingStore()
        val item = newLegacyZoneMapping("code-759", "label-256")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
