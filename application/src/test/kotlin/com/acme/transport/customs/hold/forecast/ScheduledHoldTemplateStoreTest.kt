package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledHoldTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledHoldTemplateStore()
        val item = newScheduledHoldTemplate("code-615", "title-962")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
