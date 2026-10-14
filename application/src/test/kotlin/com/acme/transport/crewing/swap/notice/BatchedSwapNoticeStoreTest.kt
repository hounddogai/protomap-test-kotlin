package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedSwapNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedSwapNoticeStore()
        val item = newBatchedSwapNotice("code-297", "title-216")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
