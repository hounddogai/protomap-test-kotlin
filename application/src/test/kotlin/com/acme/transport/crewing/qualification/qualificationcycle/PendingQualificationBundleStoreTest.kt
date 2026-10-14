package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingQualificationBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingQualificationBundleStore()
        val item = newPendingQualificationBundle("code-390", "name-642")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
