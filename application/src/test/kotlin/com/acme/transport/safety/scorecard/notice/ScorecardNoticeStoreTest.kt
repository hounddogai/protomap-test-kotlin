package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardNoticeStore()
        val item = newScorecardNotice("code-537", "label-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
