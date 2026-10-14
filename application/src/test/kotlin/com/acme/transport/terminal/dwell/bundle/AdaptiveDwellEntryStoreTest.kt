package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDwellEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDwellEntryStore()
        val item = newAdaptiveDwellEntry("code-214", "name-51")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
