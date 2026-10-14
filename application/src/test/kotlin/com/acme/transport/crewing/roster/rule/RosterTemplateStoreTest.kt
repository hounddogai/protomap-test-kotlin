package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterTemplateStore()
        val item = newRosterTemplate("code-926", "name-186")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
