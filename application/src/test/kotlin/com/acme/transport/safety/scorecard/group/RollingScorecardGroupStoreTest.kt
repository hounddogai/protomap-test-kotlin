package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingScorecardGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingScorecardGroupStore()
        val item = newRollingScorecardGroup("code-509", "title-479")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
