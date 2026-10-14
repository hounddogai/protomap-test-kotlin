package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRelayBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRelayBaselineStore()
        val item = newSeasonalRelayBaseline("code-558", "name-698")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
