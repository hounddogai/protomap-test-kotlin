package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDwellPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDwellPolicyStore()
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
