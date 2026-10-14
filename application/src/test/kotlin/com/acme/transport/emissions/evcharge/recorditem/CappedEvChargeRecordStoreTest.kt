package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedEvChargeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedEvChargeRecordStore()
        val item = newCappedEvChargeRecord("code-573", "label-633")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
