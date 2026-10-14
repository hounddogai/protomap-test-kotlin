package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldRollupStore()
        val item = newHoldRollup("code-15", "label-830")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
