package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingZoneMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingZoneMappingStore()
        val item = newRollingZoneMapping("code-505", "label-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
