package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneBundleStore()
        val item = newZoneBundle("code-336", "title-99")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
