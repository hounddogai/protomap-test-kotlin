package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockNoticeStore()
        val item = newCrossDockNotice("code-719", "name-514")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
