package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalSwapNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalSwapNoticeStore()
        val item = newRegionalSwapNotice("code-93", "label-864")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
