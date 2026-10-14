package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDwellProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDwellProfileStore()
        val item = newRegionalDwellProfile("code-99", "title-267")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
