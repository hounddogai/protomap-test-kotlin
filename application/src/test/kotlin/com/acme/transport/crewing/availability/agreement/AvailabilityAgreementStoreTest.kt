package com.acme.transport.crewing.availability.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityAgreementStore()
        val item = newAvailabilityAgreement("code-709", "label-133")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
