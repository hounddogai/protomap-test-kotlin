package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneSlotStore()
        val item = newZoneSlot("code-391", "title-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
