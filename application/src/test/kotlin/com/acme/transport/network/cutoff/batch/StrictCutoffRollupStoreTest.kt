package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCutoffRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCutoffRollupStore()
        val item = newStrictCutoffRollup("code-354", "label-997")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
