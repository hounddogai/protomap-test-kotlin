package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderRollupStore()
        val item = newTenderRollup("code-933", "label-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
