package com.acme.transport.network.hub.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubScoreStore()
        val item = newHubScore("code-937", "label-336")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
