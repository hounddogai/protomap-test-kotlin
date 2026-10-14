package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneRuleStore()
        val item = newZoneRule("code-970", "title-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
