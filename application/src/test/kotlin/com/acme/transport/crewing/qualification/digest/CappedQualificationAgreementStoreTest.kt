package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedQualificationAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedQualificationAgreementStore()
        val item = newCappedQualificationAgreement("code-54", "name-192")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
