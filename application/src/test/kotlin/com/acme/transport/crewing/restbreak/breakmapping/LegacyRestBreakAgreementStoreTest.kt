package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRestBreakAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRestBreakAgreementStore()
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
