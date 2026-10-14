package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCrossDockNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCrossDockNoticeStore()
        val item = newAdaptiveCrossDockNotice("code-102", "name-783")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
