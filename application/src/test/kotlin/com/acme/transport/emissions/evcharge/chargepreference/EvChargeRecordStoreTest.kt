package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeRecordStore()
        val item = newEvChargeRecord("code-388", "label-694")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
