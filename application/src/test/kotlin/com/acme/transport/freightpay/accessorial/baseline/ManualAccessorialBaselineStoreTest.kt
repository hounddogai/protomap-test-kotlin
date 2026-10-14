package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAccessorialBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAccessorialBaselineStore()
        val item = newManualAccessorialBaseline("code-767", "label-211")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
