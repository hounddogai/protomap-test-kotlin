package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffAgreementStore()
        val item = newCutoffAgreement("code-854", "title-697")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
