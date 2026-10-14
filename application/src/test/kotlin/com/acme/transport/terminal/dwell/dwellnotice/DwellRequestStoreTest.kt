package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellRequestStore()
        val item = newDwellRequest("code-639", "title-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
