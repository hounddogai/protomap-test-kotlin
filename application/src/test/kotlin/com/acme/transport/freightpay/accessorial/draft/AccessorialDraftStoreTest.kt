package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialDraftStore()
        val item = newAccessorialDraft("code-878", "name-769")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
