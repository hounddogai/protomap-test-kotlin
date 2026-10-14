package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientScorecardSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientScorecardSlotStore()
        val item = newLenientScorecardSlot("code-115", "name-839")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
