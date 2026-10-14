package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientQualificationClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientQualificationClaimStore()
        val item = newLenientQualificationClaim("code-250", "name-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
