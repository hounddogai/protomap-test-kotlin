package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledZoneTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledZoneTemplateStore()
        val item = newScheduledZoneTemplate("code-878", "label-44")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
