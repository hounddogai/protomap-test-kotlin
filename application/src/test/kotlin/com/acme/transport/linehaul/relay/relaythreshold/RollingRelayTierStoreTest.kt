package com.acme.transport.linehaul.relay.relaythreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRelayTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRelayTierStore()
        val item = newRollingRelayTier("code-96", "title-994")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
