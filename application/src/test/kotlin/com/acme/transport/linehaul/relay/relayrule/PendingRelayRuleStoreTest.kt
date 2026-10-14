package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRelayRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRelayRuleStore()
        val item = newPendingRelayRule("code-977", "title-382")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
