package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRosterTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRosterTemplateStore()
        val item = newScheduledRosterTemplate("code-628", "name-830")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
