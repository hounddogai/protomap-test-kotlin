package com.acme.transport.crewing.qualification.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationDigestStore()
        val item = newQualificationDigest("code-805", "title-899")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
