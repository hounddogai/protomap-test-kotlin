package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryScorecardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryScorecardWindowStore()
        val item = newPrimaryScorecardWindow("code-568", "label-428")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
