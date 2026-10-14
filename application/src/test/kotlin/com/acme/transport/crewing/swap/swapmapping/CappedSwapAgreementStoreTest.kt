package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedSwapAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedSwapAgreementStore()
        val item = newCappedSwapAgreement("code-443", "name-340")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
