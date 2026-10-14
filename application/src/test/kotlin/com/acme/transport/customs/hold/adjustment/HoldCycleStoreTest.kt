package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldCycleStore()
        val item = newHoldCycle("code-893", "label-566")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
