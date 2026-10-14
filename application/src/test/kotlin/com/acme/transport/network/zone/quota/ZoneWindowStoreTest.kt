package com.acme.transport.network.zone.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneWindowStore()
        val item = newZoneWindow("code-638", "title-784")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
