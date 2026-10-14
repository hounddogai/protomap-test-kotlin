package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneAdjustmentStore()
        val item = newZoneAdjustment("code-840", "title-78")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
