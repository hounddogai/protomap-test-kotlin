package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCapacityAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCapacityAgreementStore()
        val item = newGlobalCapacityAgreement("code-961", "title-717")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
