package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingZoneWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingZoneWindowStore()
        val item = newRollingZoneWindow("code-991", "title-558")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
