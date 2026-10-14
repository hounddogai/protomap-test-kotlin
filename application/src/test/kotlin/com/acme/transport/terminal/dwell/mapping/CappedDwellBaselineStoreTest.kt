package com.acme.transport.terminal.dwell.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDwellBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDwellBaselineStore()
        val item = newCappedDwellBaseline("code-598", "label-796")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
