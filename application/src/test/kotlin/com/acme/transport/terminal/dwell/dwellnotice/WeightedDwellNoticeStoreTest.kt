package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedDwellNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedDwellNoticeStore()
        val item = newWeightedDwellNotice("code-679", "title-648")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
