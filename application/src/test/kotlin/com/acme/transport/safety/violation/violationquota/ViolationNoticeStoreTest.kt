package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationNoticeStore()
        val item = newViolationNotice("code-515", "label-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
