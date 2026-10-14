package com.acme.transport.safety.coaching.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCoachingNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCoachingNoticeStore()
        val item = newManualCoachingNotice("code-289", "title-76")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
