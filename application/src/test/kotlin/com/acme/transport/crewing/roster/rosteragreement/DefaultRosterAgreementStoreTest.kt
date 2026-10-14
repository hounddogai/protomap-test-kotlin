package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRosterAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRosterAgreementStore()
        val item = newDefaultRosterAgreement("code-404", "label-177")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
