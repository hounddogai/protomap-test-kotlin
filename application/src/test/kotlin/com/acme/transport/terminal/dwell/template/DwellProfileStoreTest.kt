package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellProfileStore()
        val item = newDwellProfile("code-275", "title-39")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
