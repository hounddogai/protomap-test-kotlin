package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRosterNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRosterNoticeStore()
        val item = newPrimaryRosterNotice("code-362", "label-912")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
