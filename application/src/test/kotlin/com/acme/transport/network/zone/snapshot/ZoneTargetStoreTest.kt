package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneTargetStore()
        val item = newZoneTarget("code-218", "title-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
