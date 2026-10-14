package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRosterRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRosterRuleStore()
        val item = newPrimaryRosterRule("code-647", "name-387")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
