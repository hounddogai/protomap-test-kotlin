package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingTemplateStore()
        val item = newIdlingTemplate("code-261", "label-841")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
