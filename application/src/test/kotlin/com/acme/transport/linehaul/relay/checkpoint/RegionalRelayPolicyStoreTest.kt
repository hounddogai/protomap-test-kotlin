package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRelayPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRelayPolicyStore()
        val item = newRegionalRelayPolicy("code-495", "name-160")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
