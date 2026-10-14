package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDwellAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDwellAdjustmentStore()
        val item = newStrictDwellAdjustment("code-817", "label-790")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
