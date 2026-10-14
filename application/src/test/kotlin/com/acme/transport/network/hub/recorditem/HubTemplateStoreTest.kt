package com.acme.transport.network.hub.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubTemplateStore()
        val item = newHubTemplate("code-287", "title-351")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
