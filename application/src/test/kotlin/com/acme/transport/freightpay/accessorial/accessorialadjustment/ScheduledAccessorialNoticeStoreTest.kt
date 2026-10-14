package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAccessorialNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAccessorialNoticeStore()
        val item = newScheduledAccessorialNotice("code-155", "name-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
