package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRestBreakNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRestBreakNoticeStore()
        val item = newCappedRestBreakNotice("code-732", "name-139")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
