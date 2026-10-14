package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalQualificationBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalQualificationBundleStore()
        val item = newGlobalQualificationBundle("code-555", "name-306")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
