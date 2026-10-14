package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationClaimStore()
        val item = newQualificationClaim("code-515", "title-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
