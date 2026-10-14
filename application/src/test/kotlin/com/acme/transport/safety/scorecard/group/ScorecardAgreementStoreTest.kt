package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardAgreementStore()
        val item = newScorecardAgreement("code-705", "name-179")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
