package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneMappingStore()
        val item = newZoneMapping("code-413", "name-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
