package com.acme.transport.lastmile.attempt.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAttemptNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAttemptNoticeStore()
        val item = newDefaultAttemptNotice("code-156", "name-37")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
