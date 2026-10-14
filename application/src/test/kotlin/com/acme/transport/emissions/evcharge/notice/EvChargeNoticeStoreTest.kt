package com.acme.transport.emissions.evcharge.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeNoticeStore()
        val item = newEvChargeNotice("code-150", "name-247")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
