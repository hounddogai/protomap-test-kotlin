package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellTemplateStore()
        val item = newDwellTemplate("code-536", "name-692")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
