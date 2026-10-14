package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalQualificationNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalQualificationNoticeStore()
        val item = newRegionalQualificationNotice("code-230", "title-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
