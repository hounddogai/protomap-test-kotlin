package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTransitTimeNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTransitTimeNoticeStore()
        val item = newStrictTransitTimeNotice("code-890", "title-738")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
