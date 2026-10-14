package com.acme.transport.safety.coaching.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCoachingAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCoachingAgreementStore()
        val item = newDefaultCoachingAgreement("code-874", "label-526")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
