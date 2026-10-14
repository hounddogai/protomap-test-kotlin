package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalClearanceNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalClearanceNoticeStore()
        val item = newSeasonalClearanceNotice("code-635", "title-314")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
