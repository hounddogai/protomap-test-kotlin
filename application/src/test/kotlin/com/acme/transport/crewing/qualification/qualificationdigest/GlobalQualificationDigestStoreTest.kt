package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalQualificationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalQualificationDigestStore()
        val item = newGlobalQualificationDigest("code-546", "name-363")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
