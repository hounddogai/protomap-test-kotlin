package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDwellMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDwellMilestoneStore()
        val item = newLenientDwellMilestone("code-158", "label-754")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
