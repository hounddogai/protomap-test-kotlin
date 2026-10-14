package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRedeliveryNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRedeliveryNoticeStore()
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
