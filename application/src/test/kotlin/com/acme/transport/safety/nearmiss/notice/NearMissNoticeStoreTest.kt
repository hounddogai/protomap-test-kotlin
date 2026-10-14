package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissNoticeStore()
        val item = newNearMissNotice("code-962", "title-789")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
