package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldAgreementStore()
        val item = newHoldAgreement("code-219", "label-640")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
