package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellBaselineStore()
        val item = newDwellBaseline("code-736", "title-107")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
