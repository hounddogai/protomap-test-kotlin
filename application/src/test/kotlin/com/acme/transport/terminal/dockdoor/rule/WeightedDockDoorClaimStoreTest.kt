package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDockDoorClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDockDoorClaimStore()
        val item = newWeightedDockDoorClaim("code-255", "label-569")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
