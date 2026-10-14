package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalZoneTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalZoneTemplateStore()
        val item = newGlobalZoneTemplate("code-738", "title-234")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
