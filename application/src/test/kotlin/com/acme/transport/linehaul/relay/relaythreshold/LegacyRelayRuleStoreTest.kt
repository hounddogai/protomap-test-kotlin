package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRelayRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRelayRuleStore()
        val item = newLegacyRelayRule("code-537", "title-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
