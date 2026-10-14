package com.acme.transport.linehaul.tender.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderTargetStore()
        val item = newTenderTarget("code-932", "name-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
