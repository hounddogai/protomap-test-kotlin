package com.acme.transport.terminal.crossdock.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockMilestoneStore()
        val item = newCrossDockMilestone("code-917", "label-11")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
