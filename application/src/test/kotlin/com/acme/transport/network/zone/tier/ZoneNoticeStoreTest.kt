package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneNoticeStore()
        val item = newZoneNotice("code-373", "title-920")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
