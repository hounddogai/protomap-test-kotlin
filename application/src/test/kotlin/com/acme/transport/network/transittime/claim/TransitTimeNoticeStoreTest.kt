package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeNoticeStore()
        val item = newTransitTimeNotice("code-742", "title-945")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
