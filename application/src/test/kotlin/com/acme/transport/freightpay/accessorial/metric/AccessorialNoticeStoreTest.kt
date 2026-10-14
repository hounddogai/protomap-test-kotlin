package com.acme.transport.freightpay.accessorial.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialNoticeStore()
        val item = newAccessorialNotice("code-421", "label-329")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
