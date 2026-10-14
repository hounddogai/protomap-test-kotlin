package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledHubRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledHubRuleStore()
        val item = newScheduledHubRule("code-624", "name-275")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
