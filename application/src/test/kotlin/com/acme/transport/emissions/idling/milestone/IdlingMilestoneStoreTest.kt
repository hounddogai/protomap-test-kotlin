package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingMilestoneStore()
        val item = newIdlingMilestone("code-775", "title-393")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
