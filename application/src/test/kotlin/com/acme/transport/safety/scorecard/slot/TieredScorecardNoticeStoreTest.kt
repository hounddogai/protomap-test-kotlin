package com.acme.transport.safety.scorecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredScorecardNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredScorecardNoticeStore()
        val item = newTieredScorecardNotice("code-236", "title-931")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
