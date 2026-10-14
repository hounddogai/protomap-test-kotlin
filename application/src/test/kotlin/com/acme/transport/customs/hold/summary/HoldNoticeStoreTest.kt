package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldNoticeStore()
        val item = newHoldNotice("code-126", "title-940")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
