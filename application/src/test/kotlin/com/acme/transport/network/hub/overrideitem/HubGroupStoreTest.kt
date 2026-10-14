package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubGroupStore()
        val item = newHubGroup("code-665", "title-308")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
