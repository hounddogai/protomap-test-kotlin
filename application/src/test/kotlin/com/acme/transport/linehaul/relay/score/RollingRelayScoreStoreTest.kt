package com.acme.transport.linehaul.relay.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRelayScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRelayScoreStore()
        val item = newRollingRelayScore("code-272", "name-991")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
