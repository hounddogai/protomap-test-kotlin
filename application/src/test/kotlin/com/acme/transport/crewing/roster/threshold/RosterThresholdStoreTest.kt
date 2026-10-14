package com.acme.transport.crewing.roster.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterThresholdStore()
        val item = newRosterThreshold("code-635", "title-76")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
