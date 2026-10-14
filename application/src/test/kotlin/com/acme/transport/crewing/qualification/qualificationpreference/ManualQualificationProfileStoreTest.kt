package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualQualificationProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualQualificationProfileStore()
        val item = newManualQualificationProfile("code-598", "title-229")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
