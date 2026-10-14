package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTenderBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTenderBatchStore()
        val item = newSeasonalTenderBatch("code-101", "title-655")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
