package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTransitTimeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTransitTimeTemplateStore()
        val item = newGlobalTransitTimeTemplate("code-680", "title-424")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
