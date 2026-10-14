package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingZoneMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingZoneMappingStore()
        val item = newPendingZoneMapping("code-271", "label-999")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
