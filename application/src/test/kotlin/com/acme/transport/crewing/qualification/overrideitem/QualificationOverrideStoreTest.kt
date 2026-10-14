package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationOverrideStore()
        val item = newQualificationOverride("code-76", "title-188")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
