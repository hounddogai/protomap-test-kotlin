package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireTemplateStore()
        val item = newTireTemplate("code-212", "name-340")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
