package com.acme.transport.terminal.dockdoor.doorthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDockDoorExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDockDoorExceptionStore()
        val item = newWeightedDockDoorException("code-816", "title-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
