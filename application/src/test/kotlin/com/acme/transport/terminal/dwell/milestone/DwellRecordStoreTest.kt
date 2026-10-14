package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellRecordStore()
        val item = newDwellRecord("code-51", "title-717")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
