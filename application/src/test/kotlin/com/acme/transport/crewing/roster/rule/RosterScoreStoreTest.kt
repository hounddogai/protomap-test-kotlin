package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterScoreStore()
        val item = newRosterScore("code-786", "label-248")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
