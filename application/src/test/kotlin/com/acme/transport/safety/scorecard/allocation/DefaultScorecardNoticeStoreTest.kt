package com.acme.transport.safety.scorecard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultScorecardNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultScorecardNoticeStore()
        val item = newDefaultScorecardNotice("code-616", "title-169")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
