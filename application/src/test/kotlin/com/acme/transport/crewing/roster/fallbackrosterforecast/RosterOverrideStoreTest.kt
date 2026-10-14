package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterOverrideStore()
        val item = newRosterOverride("code-157", "label-756")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
