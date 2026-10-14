package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientZoneNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientZoneNoticeStore()
        val item = newLenientZoneNotice("code-648", "name-563")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
