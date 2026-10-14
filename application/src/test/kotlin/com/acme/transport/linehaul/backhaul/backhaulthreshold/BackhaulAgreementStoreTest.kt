package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulAgreementStore()
        val item = newBackhaulAgreement("code-944", "label-283")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
