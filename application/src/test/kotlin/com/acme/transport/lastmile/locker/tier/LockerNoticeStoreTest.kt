package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerNoticeStore()
        val item = newLockerNotice("code-640", "name-282")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
