package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledServiceAreaNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledServiceAreaNoticeStore()
        val item = newScheduledServiceAreaNotice("code-887", "name-352")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
