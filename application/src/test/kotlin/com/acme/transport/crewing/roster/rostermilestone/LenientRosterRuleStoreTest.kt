package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRosterRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRosterRuleStore()
        val item = newLenientRosterRule("code-248", "title-281")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
