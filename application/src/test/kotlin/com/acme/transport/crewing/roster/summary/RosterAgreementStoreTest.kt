package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterAgreementStore()
        val item = newRosterAgreement("code-515", "name-950")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
