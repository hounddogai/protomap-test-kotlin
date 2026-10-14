package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellOverrideStore()
        val item = newDwellOverride("code-536", "title-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
