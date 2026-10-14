package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDropAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDropAgreementStore()
        val item = newSeasonalDropAgreement("code-430", "label-212")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
