package com.acme.transport.safety.coaching.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingNoticeStore()
        val item = newCoachingNotice("code-319", "name-453")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
