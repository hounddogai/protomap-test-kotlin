package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDropTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDropTargetStore()
        val item = newWeightedDropTarget("code-878", "label-808")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
