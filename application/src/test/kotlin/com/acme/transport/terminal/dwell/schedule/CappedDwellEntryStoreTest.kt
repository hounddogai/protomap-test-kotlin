package com.acme.transport.terminal.dwell.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDwellEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDwellEntryStore()
        val item = newCappedDwellEntry("code-208", "title-186")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
