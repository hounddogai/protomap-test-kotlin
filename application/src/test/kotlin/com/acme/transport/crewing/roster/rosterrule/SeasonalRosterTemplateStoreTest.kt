package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRosterTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRosterTemplateStore()
        val item = newSeasonalRosterTemplate("code-218", "label-14")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
