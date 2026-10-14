package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaNoticeStore()
        val item = newServiceAreaNotice("code-699", "name-340")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
