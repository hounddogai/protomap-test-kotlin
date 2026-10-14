package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryQualificationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryQualificationRecordStore()
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
